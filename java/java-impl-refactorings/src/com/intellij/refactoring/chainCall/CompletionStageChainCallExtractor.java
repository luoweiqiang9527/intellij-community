/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.refactoring.chainCall;

import com.intellij.psi.*;
import com.siyeh.ig.callMatcher.CallMatcher;
import org.jetbrains.annotations.NotNull;

public final class CompletionStageChainCallExtractor implements ChainCallExtractor {
  private static final CallMatcher NEXT_CALL =
    CallMatcher.instanceCall("java.util.concurrent.CompletionStage", "thenApply", "thenAccept", "thenCompose").parameterCount(1);

  @Override
  public boolean canExtractChainCall(@NotNull PsiMethodCallExpression call, @NotNull PsiExpression expression, PsiType expressionType) {
    if (expressionType instanceof PsiPrimitiveType) return false;
    if (call.getMethodExpression().getQualifierExpression() == null) return false;
    return NEXT_CALL.test(call);
  }

  @Override
  public String getMethodName(PsiVariable variable, PsiExpression expression, PsiType expressionType) {
    return "thenApply";
  }
}
