package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.binaryOperations.strategy;

import com.intellij.codeInsight.PsiEquivalenceUtil;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.BinaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

final public class IdenticalOperandsStrategy {
    private static final String message = "Left and right operands are identical.";

    private final static Set<IElementType> operationsToCheck = new HashSet<>();
    static {
        operationsToCheck.add(PhpTokenTypes.opEQUAL);
        operationsToCheck.add(PhpTokenTypes.opIDENTICAL);
        operationsToCheck.add(PhpTokenTypes.opNOT_EQUAL);
        operationsToCheck.add(PhpTokenTypes.opNOT_IDENTICAL);
        operationsToCheck.add(PhpTokenTypes.opGREATER);
        operationsToCheck.add(PhpTokenTypes.opGREATER_OR_EQUAL);
        operationsToCheck.add(PhpTokenTypes.opLESS);
        operationsToCheck.add(PhpTokenTypes.opLESS_OR_EQUAL);
        operationsToCheck.add(PhpTokenTypes.kwINSTANCEOF);
    }

    public static boolean apply(@NotNull BinaryExpression expression, @NotNull ProblemsHolder holder) {
        final PsiElement left  = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getLeftOperand());
        final PsiElement right = ExpressionSemanticUtil.getExpressionTroughParenthesis(expression.getRightOperand());
        if (null == left || null == right) {
            return false;
        }

        final IElementType operation = expression.getOperationType();
        if (operationsToCheck.contains(operation) && PsiEquivalenceUtil.areElementsEquivalent(left, right)) {
            holder.registerProblem(expression, message, ProblemHighlightType.GENERIC_ERROR);
            return true;
        }

        return false;
    }
}
