/* LanguageTool, a natural language style checker 
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool;

import org.junit.Test;
import org.languagetool.JLanguageTool.Level;
import org.languagetool.JLanguageTool.Mode;
import org.languagetool.JLanguageTool.ParagraphHandling;
import org.languagetool.language.French;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.FakeRule;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class JLanguageToolTest {

  @Test
  public void testLanguageDependentFilter() throws IOException {
    Language lang = new French();
    JLanguageTool tool = new JLanguageTool(lang);

    // picky mode: suggestions with typographical apostrophes
    AnalyzedSentence analyzedSentence = tool.getAnalyzedSentence("De homme");
    AnnotatedText annotatedText = new AnnotatedTextBuilder().addText(analyzedSentence.getText()).build();
    RuleMatchListener listener = null;
    List<RuleMatch> matches = tool.check(annotatedText, true, ParagraphHandling.NORMAL, listener, Mode.ALL,
        Level.PICKY);
    assertEquals(1, matches.size());
    assertEquals("[D'homme]", matches.get(0).getSuggestedReplacements().toString());

    // normal mode: suggestions with straight apostrophes
    analyzedSentence = tool.getAnalyzedSentence("De homme");
    annotatedText = new AnnotatedTextBuilder().addText(analyzedSentence.getText()).build();
    listener = null;
    matches = tool.check(annotatedText, true, ParagraphHandling.NORMAL, listener, Mode.ALL, Level.DEFAULT);
    assertEquals(1, matches.size());
    assertEquals("[D'homme]", matches.get(0).getSuggestedReplacements().toString());
    
    tool.check("vrai/faux avec explication : Les droits d'accès, également appelés permissions ou autorisations, sont des règles définissant");

  }

  @Test
  public void testMultitokenSpeller() throws IOException {
    Language lang = new French();
    assertEquals("[gréco-romain, Gréco-Romain]", lang.getMultitokenSpeller().getSuggestions("greco-romain").toString());
    assertEquals("[Rimski-Korsakov]", lang.getMultitokenSpeller().getSuggestions("Rinsky-Korsakov").toString());
    assertEquals("[Nikolaï Rimski-Korsakov]", lang.getMultitokenSpeller().getSuggestions("Nikolai Rimski-Korsakov").toString());
  }

  @Test
  public void testMatchfiltering() throws IOException {
    Language lang = new French();
    JLanguageTool lt = new JLanguageTool(lang);
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("C'est Madame Curie.");
    RuleMatch ruleMatch = new RuleMatch(new FakeRule("AI_FR_GGEC_REPLACEMENT_ORTHOGRAPHY_UPPERCASE_MADAME_MADAME"), analyzedSentence,6, 12, "Possible error");
    ruleMatch.setSuggestedReplacement("madame");
    List<RuleMatch> ruleMatches = new ArrayList<>();
    ruleMatches.add(ruleMatch);
    List<RuleMatch> filteredRuleMatches =lang.mergeSuggestions(ruleMatches, null, null);
    assertEquals("Un usage différent des majuscules et des minuscules est recommandé.", filteredRuleMatches.get(0).getMessage());
    assertEquals("Majuscules et minuscules", filteredRuleMatches.get(0).getShortMessage());
    assertEquals(ITSIssueType.Typographical, filteredRuleMatches.get(0).getRule().getLocQualityIssueType());
    assertEquals("CASING", filteredRuleMatches.get(0).getRule().getCategory().getId().toString());
    assertEquals("AI_FR_GGEC_REPLACEMENT_ORTHOGRAPHY_UPPERCASE_MADAME_MADAME", filteredRuleMatches.get(0).getRule().getFullId());
    assertEquals("AI_FR_GGEC_REPLACEMENT_CASING_UPPERCASE_MADAME_MADAME", filteredRuleMatches.get(0).getSpecificRuleId());
  }
}
