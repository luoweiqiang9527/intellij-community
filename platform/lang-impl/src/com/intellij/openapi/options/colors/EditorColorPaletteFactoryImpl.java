// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.openapi.options.colors;


import com.intellij.lang.Language;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.EditorColorPalette;
import com.intellij.openapi.editor.colors.EditorColorPaletteFactory;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.options.colors.pages.GeneralColorsPage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class EditorColorPaletteFactoryImpl extends EditorColorPaletteFactory {
  
  @Override
  public EditorColorPalette getPalette(@NotNull EditorColorsScheme scheme, @Nullable Language language) {
    return new ColorPagesPalette(scheme, language);
  }

  private static final class ColorPagesPalette extends EditorColorPalette {

    private final @Nullable Language myLanguage;

    ColorPagesPalette(@NotNull EditorColorsScheme colorsScheme, @Nullable Language language) {
      super(colorsScheme);
      myLanguage = language;
    }

    @Override
    protected Collection<TextAttributesKey> getTextAttributeKeys(boolean filterOutRainbowNonConflictingAttrKeys) {
      Set<TextAttributesKey> textAttributesKeys = new HashSet<>();
      for (ColorSettingsPage page : ColorSettingsPages.getInstance().getRegisteredPages()) {
        Language pageLanguage = guessPageLanguage(page);
        if (
          myLanguage == null && pageLanguage == null ||
          myLanguage != null && (myLanguage.is(pageLanguage) ||
                                 myLanguage.is(Language.ANY) && pageIsGoodForAnyLanguage(filterOutRainbowNonConflictingAttrKeys, page))
          ) {
          for (AttributesDescriptor descriptor : page.getAttributeDescriptors()) {
            final TextAttributesKey textAttributesKey = descriptor.getKey();
            if (filterOutRainbowNonConflictingAttrKeys
                && (textAttributesKey == DefaultLanguageHighlighterColors.LOCAL_VARIABLE
                    || textAttributesKey == DefaultLanguageHighlighterColors.PARAMETER)) {
              continue;              
            }
            if (!filterOutRainbowNonConflictingAttrKeys ||
                pageLanguage == null ||
                page instanceof RainbowColorSettingsPage && !((RainbowColorSettingsPage)page).isRainbowType(textAttributesKey)) {
              textAttributesKeys.add(textAttributesKey);
            }
          }
          if (page instanceof GeneralColorsPage) {
            // collecting HighlightInfoType info
            final Map<String, TextAttributesKey> map = page.getAdditionalHighlightingTagToDescriptorMap();
            if (map != null) {
              textAttributesKeys.addAll(map.values());
            }
          }
        }
      }
      return textAttributesKeys;
    }

    private static @Nullable Language guessPageLanguage(@NotNull ColorSettingsPage page) {
      for (Language language : Language.getRegisteredLanguages()) {
        if (page instanceof RainbowColorSettingsPage && language.is(((RainbowColorSettingsPage)page).getLanguage()) ||
            page.getDisplayName().equals(language.getDisplayName())) {
          return language;
        }
      }
      return null;
    }
  }

  @Contract(value = "false, _ -> true", pure = true)
  private static boolean pageIsGoodForAnyLanguage(boolean filterOutRainbowNonConflictingAttrKeys,
                                                  @NotNull ColorSettingsPage page) {
    return !filterOutRainbowNonConflictingAttrKeys
           || page instanceof GeneralColorsPage
           || page instanceof RainbowColorSettingsPage;
  }
}
