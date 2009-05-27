/*
 * Copyright 2002-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.expression.spel.ast;

import org.springframework.expression.EvaluationException;
import org.springframework.expression.TypedValue;
import org.springframework.expression.common.ExpressionUtils;
import org.springframework.expression.spel.ExpressionState;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessages;
import org.springframework.expression.spel.SpelNode;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * The common supertype of all AST nodes in a parsed Spring Expression Language format expression.
 *
 * @author Andy Clement
 * @since 3.0
 */
public abstract class SpelNodeImpl implements SpelNode, CommonTypeDescriptors {
	
	private static SpelNodeImpl[] NO_CHILDREN = new SpelNodeImpl[0];
	
	protected int pos; // start = top 16bits, end = bottom 16bits
	protected SpelNodeImpl[] children = SpelNodeImpl.NO_CHILDREN;
	
	public SpelNodeImpl(int pos, SpelNodeImpl... operands) {
		this.pos = pos;
		// pos combines start and end so can never be zero because tokens cannot be zero length
		assert pos!=0;
		if (operands!=null && operands.length>0) {
			this.children = operands;
		}
	}

	public final Object getValue(ExpressionState expressionState) throws EvaluationException {
		if (expressionState != null) {
			return getValueInternal(expressionState).getValue();
		} else {
			return getValue(new ExpressionState(new StandardEvaluationContext()));
		}
	}

	// by default Ast nodes are not writable
	public boolean isWritable(ExpressionState expressionState) throws EvaluationException {
		return false;
	}

	public void setValue(ExpressionState expressionState, Object newValue) throws EvaluationException {
		throw new SpelEvaluationException(getStartPosition(), SpelMessages.SETVALUE_NOT_SUPPORTED, getClass());
	}

	public SpelNode getChild(int index) {
		return children[index];
	}
	
	public int getChildCount() {
		return children.length;
	}

	public Class<?> getObjectClass(Object obj) {
		if (obj == null) {
			return null;
		}
		return (obj instanceof Class ? ((Class<?>) obj) : obj.getClass());
	}

	@SuppressWarnings("unchecked")
	protected final <T> T getValue(ExpressionState state, Class<T> desiredReturnType) throws EvaluationException {
		Object result = getValueInternal(state).getValue();
		if (result != null && desiredReturnType != null) {
			Class<?> resultType = result.getClass();
			if (desiredReturnType.isAssignableFrom(resultType)) {
				return (T) result;
			}
			// Attempt conversion to the requested type, may throw an exception
			return ExpressionUtils.convert(state.getEvaluationContext(), result, desiredReturnType);
		}
		return (T) result;
	}

	public abstract TypedValue getValueInternal(ExpressionState expressionState) throws EvaluationException;

	public abstract String toStringAST();

	public int getStartPosition() {
		return (pos>>16);
	}

	public int getEndPosition() {
		return (pos&0xffff);
	} 

}
