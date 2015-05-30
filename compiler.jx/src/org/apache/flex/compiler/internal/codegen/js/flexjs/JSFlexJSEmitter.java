/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.flex.compiler.internal.codegen.js.flexjs;

import java.io.FilterWriter;

import org.apache.flex.compiler.asdoc.flexjs.ASDocComment;
import org.apache.flex.compiler.codegen.IDocEmitter;
import org.apache.flex.compiler.codegen.js.flexjs.IJSFlexJSEmitter;
import org.apache.flex.compiler.common.ASModifier;
import org.apache.flex.compiler.common.IMetaInfo;
import org.apache.flex.compiler.definitions.IClassDefinition;
import org.apache.flex.compiler.definitions.IDefinition;
import org.apache.flex.compiler.definitions.IFunctionDefinition;
import org.apache.flex.compiler.definitions.IPackageDefinition;
import org.apache.flex.compiler.definitions.ITypeDefinition;
import org.apache.flex.compiler.internal.codegen.as.ASEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.JSEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitter;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogEmitterTokens;
import org.apache.flex.compiler.internal.codegen.js.jx.BinaryOperatorEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.ClassEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.FieldEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.FunctionCallEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.IdentifierEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.MemberAccessEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.PackageFooterEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.PackageHeaderEmitter;
import org.apache.flex.compiler.internal.codegen.js.jx.SuperCallEmitter;
import org.apache.flex.compiler.internal.tree.as.FunctionNode;
import org.apache.flex.compiler.internal.tree.as.RegExpLiteralNode;
import org.apache.flex.compiler.internal.tree.as.SetterNode;
import org.apache.flex.compiler.tree.ASTNodeID;
import org.apache.flex.compiler.tree.as.IASNode;
import org.apache.flex.compiler.tree.as.IAccessorNode;
import org.apache.flex.compiler.tree.as.IBinaryOperatorNode;
import org.apache.flex.compiler.tree.as.IClassNode;
import org.apache.flex.compiler.tree.as.IDefinitionNode;
import org.apache.flex.compiler.tree.as.IExpressionNode;
import org.apache.flex.compiler.tree.as.IForLoopNode;
import org.apache.flex.compiler.tree.as.IFunctionCallNode;
import org.apache.flex.compiler.tree.as.IFunctionNode;
import org.apache.flex.compiler.tree.as.IGetterNode;
import org.apache.flex.compiler.tree.as.IIdentifierNode;
import org.apache.flex.compiler.tree.as.ILiteralNode;
import org.apache.flex.compiler.tree.as.ILiteralNode.LiteralType;
import org.apache.flex.compiler.tree.as.IMemberAccessExpressionNode;
import org.apache.flex.compiler.tree.as.IParameterNode;
import org.apache.flex.compiler.tree.as.ISetterNode;
import org.apache.flex.compiler.tree.as.ITypedExpressionNode;
import org.apache.flex.compiler.tree.as.IVariableExpressionNode;
import org.apache.flex.compiler.tree.as.IVariableNode;

/**
 * Concrete implementation of the 'goog' JavaScript production.
 * 
 * @author Michael Schmalle
 * @author Erik de Bruin
 */
public class JSFlexJSEmitter extends JSGoogEmitter implements IJSFlexJSEmitter
{

    private int foreachLoopCounter = 0;

    private JSFlexJSDocEmitter docEmitter = null;

    private PackageHeaderEmitter packageHeaderEmitter;
    private PackageFooterEmitter packageFooterEmitter;

    private ClassEmitter classEmitter;
    private FieldEmitter fieldEmitter;
    private FunctionCallEmitter functionCallEmitter;
    private SuperCallEmitter superCallEmitter;
    private MemberAccessEmitter memberAccessEmitter;
    private BinaryOperatorEmitter binaryOperatorEmitter;
    private IdentifierEmitter identifierEmitter;

    public ClassEmitter getClassEmiter()
    {
        return classEmitter;
    }

    @Override
    public IDocEmitter getDocEmitter()
    {
        if (docEmitter == null)
            docEmitter = new JSFlexJSDocEmitter(this);
        return docEmitter;
    }

    public JSFlexJSEmitter(FilterWriter out)
    {
        super(out);

        packageHeaderEmitter = new PackageHeaderEmitter(this);
        packageFooterEmitter = new PackageFooterEmitter(this);

        classEmitter = new ClassEmitter(this);
        fieldEmitter = new FieldEmitter(this);
        functionCallEmitter = new FunctionCallEmitter(this);
        superCallEmitter = new SuperCallEmitter(this);
        memberAccessEmitter = new MemberAccessEmitter(this);
        binaryOperatorEmitter = new BinaryOperatorEmitter(this);
        identifierEmitter = new IdentifierEmitter(this);
    }

    @Override
    protected void writeIndent()
    {
        write(JSFlexJSEmitterTokens.INDENT);
    }

    @Override
    protected String getIndent(int numIndent)
    {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numIndent; i++)
            sb.append(JSFlexJSEmitterTokens.INDENT.getToken());
        return sb.toString();
    }

    @Override
    protected void emitMemberName(IDefinitionNode node)
    {
        write(node.getName());
    }

    @Override
    public void emitClass(IClassNode node)
    {
        classEmitter.emit(node);
    }

    @Override
    public void emitField(IVariableNode node)
    {
        fieldEmitter.emit(node);
    }

    public void emitBindableVarDefineProperty(String name, IClassDefinition cdef)
    {
        // 'PropName': {
        writeNewline("/** @expose */");
        writeNewline(name + ASEmitterTokens.COLON.getToken()
                + ASEmitterTokens.SPACE.getToken()
                + ASEmitterTokens.BLOCK_OPEN.getToken());
        indentPush();
        writeNewline("/** @this {"
                + formatQualifiedName(cdef.getQualifiedName()) + "} */");
        writeNewline(ASEmitterTokens.GET.getToken()
                + ASEmitterTokens.COLON.getToken()
                + ASEmitterTokens.SPACE.getToken()
                + ASEmitterTokens.FUNCTION.getToken()
                + ASEmitterTokens.PAREN_OPEN.getToken()
                + ASEmitterTokens.PAREN_CLOSE.getToken()
                + ASEmitterTokens.SPACE.getToken()
                + ASEmitterTokens.BLOCK_OPEN.getToken());
        writeNewline(ASEmitterTokens.RETURN.getToken()
                + ASEmitterTokens.SPACE.getToken()
                + ASEmitterTokens.THIS.getToken()
                + ASEmitterTokens.MEMBER_ACCESS.getToken() + name + "_"
                + ASEmitterTokens.SEMICOLON.getToken());
        indentPop();
        writeNewline(ASEmitterTokens.BLOCK_CLOSE.getToken()
                + ASEmitterTokens.COMMA.getToken());
        writeNewline();
        writeNewline("/** @this {"
                + formatQualifiedName(cdef.getQualifiedName()) + "} */");
        writeNewline(ASEmitterTokens.SET.getToken()
                + ASEmitterTokens.COLON.getToken()
                + ASEmitterTokens.SPACE.getToken()
                + ASEmitterTokens.FUNCTION.getToken()
                + ASEmitterTokens.PAREN_OPEN.getToken() + "value"
                + ASEmitterTokens.PAREN_CLOSE.getToken()
                + ASEmitterTokens.SPACE.getToken()
                + ASEmitterTokens.BLOCK_OPEN.getToken());
        writeNewline("if (value != " + ASEmitterTokens.THIS.getToken()
                + ASEmitterTokens.MEMBER_ACCESS.getToken() + name + "_) {");
        writeNewline("    var oldValue = " + ASEmitterTokens.THIS.getToken()
                + ASEmitterTokens.MEMBER_ACCESS.getToken() + name + "_"
                + ASEmitterTokens.SEMICOLON.getToken());
        writeNewline("    " + ASEmitterTokens.THIS.getToken()
                + ASEmitterTokens.MEMBER_ACCESS.getToken() + name
                + "_ = value;");
        writeNewline("    this.dispatchEvent(org_apache_flex_events_ValueChangeEvent.createUpdateEvent(");
        writeNewline("         this, \"" + name + "\", oldValue, value));");
        writeNewline("}");
        write(ASEmitterTokens.BLOCK_CLOSE.getToken());
        write(ASEmitterTokens.BLOCK_CLOSE.getToken());
    }

    @Override
    public void emitAccessors(IAccessorNode node)
    {
        if (node.getNodeID() == ASTNodeID.GetterID)
        {
            emitGetAccessor((IGetterNode) node);
        }
        else if (node.getNodeID() == ASTNodeID.SetterID)
        {
            emitSetAccessor((ISetterNode) node);
        }
    }

    @Override
    public void emitFunctionCall(IFunctionCallNode node)
    {
        functionCallEmitter.emit(node);
    }

    //--------------------------------------------------------------------------

    @Override
    protected void emitSelfReference(IFunctionNode node)
    {
        // we don't want 'var self = this;' in FlexJS
        // unless there are anonymous functions
        if (node.containsAnonymousFunctions())
            super.emitSelfReference(node);
    }

    @Override
    public void emitIdentifier(IIdentifierNode node)
    {
        // TODO (mschmalle) remove when project field is removed
        if (project == null)
            project = getWalker().getProject();

        identifierEmitter.emit(node);
    }

    //--------------------------------------------------------------------------

    @Override
    public void emitSuperCall(IASNode node, String type)
    {
        superCallEmitter.emit(node, type);
    }

    @Override
    public void emitBinaryOperator(IBinaryOperatorNode node)
    {
        binaryOperatorEmitter.emit(node);
    }

    public void emitIsAs(IExpressionNode left, IExpressionNode right,
            ASTNodeID id, boolean coercion)
    {
        // project is null in unit tests
        IDefinition dnode = project != null ? (right).resolve(project) : null;
        if (id != ASTNodeID.Op_IsID && dnode != null)
        {
            // find the function node
            IFunctionNode functionNode = (IFunctionNode) left
                    .getAncestorOfType(IFunctionNode.class);
            if (functionNode != null) // can be null in synthesized binding code
            {
                ASDocComment asDoc = (ASDocComment) functionNode
                        .getASDocComment();
                if (asDoc != null)
                {
                    String asDocString = asDoc.commentNoEnd();
                    String ignoreToken = JSFlexJSEmitterTokens.IGNORE_COERCION
                            .getToken();
                    boolean ignore = false;
                    int ignoreIndex = asDocString.indexOf(ignoreToken);
                    while (ignoreIndex != -1)
                    {
                        String ignorable = asDocString.substring(ignoreIndex
                                + ignoreToken.length());
                        int endIndex = ignorable.indexOf("\n");
                        ignorable = ignorable.substring(0, endIndex);
                        ignorable = ignorable.trim();
                        String rightSide = dnode.getQualifiedName();
                        if (ignorable.equals(rightSide))
                        {
                            ignore = true;
                            break;
                        }
                        ignoreIndex = asDocString.indexOf(ignoreToken,
                                ignoreIndex + ignoreToken.length());
                    }
                    if (ignore)
                    {
                        getWalker().walk(left);
                        return;
                    }
                }
            }
        }
        write(JSFlexJSEmitterTokens.LANGUAGE_QNAME);
        write(ASEmitterTokens.MEMBER_ACCESS);
        if (id == ASTNodeID.Op_IsID)
            write(ASEmitterTokens.IS);
        else
            write(ASEmitterTokens.AS);
        write(ASEmitterTokens.PAREN_OPEN);
        getWalker().walk(left);
        writeToken(ASEmitterTokens.COMMA);

        if (dnode != null)
            write(formatQualifiedName(dnode.getQualifiedName()));
        else
            getWalker().walk(right);

        if (coercion)
        {
            writeToken(ASEmitterTokens.COMMA);
            write(ASEmitterTokens.TRUE);
        }

        write(ASEmitterTokens.PAREN_CLOSE);
    }

    @Override
    public void emitMemberAccessExpression(IMemberAccessExpressionNode node)
    {
        // TODO (mschmalle) remove when project field is removed
        if (project == null)
            project = getWalker().getProject();

        memberAccessEmitter.emit(node);
    }

    @Override
    public void emitGetAccessor(IGetterNode node)
    {
       classEmitter.getGetSetEmitter().emitGet(node);
    }

    @Override
    public void emitSetAccessor(ISetterNode node)
    {
        classEmitter.getGetSetEmitter().emitSet(node);
    }

    @Override
    protected void emitObjectDefineProperty(IAccessorNode node)
    {
        //TODO: ajh  is this method needed anymore?

        FunctionNode fn = (FunctionNode) node;
        fn.parseFunctionBody(getProblems());

        IFunctionDefinition definition = node.getDefinition();
        ITypeDefinition type = (ITypeDefinition) definition.getParent();

        // ToDo (erikdebruin): add VF2JS conditional -> only use check during full SDK compilation
        if (type == null)
            return;

        boolean isBindableSetter = false;
        if (node instanceof SetterNode)
        {
            IMetaInfo[] metaInfos = null;
            metaInfos = node.getMetaInfos();
            for (IMetaInfo metaInfo : metaInfos)
            {
                String name = metaInfo.getTagName();
                if (name.equals("Bindable")
                        && metaInfo.getAllAttributes().length == 0)
                {
                    isBindableSetter = true;
                    break;
                }
            }
        }
        if (isBindableSetter)
        {
            getDoc().emitMethodDoc(fn, project);
            write(formatQualifiedName(type.getQualifiedName()));
            if (!node.hasModifier(ASModifier.STATIC))
            {
                write(ASEmitterTokens.MEMBER_ACCESS);
                write(JSEmitterTokens.PROTOTYPE);
            }

            write(ASEmitterTokens.MEMBER_ACCESS);
            write("__bindingWrappedSetter__");
            writeToken(node.getName());
            writeToken(ASEmitterTokens.EQUAL);
            write(ASEmitterTokens.FUNCTION);
            emitParameters(node.getParameterNodes());
            //writeNewline();
            emitMethodScope(node.getScopedNode());
        }
        super.emitObjectDefineProperty(node);
    }

    @Override
    public void emitDefinePropertyFunction(IAccessorNode node)
    {
        boolean isBindableSetter = false;
        if (node instanceof SetterNode)
        {
            IMetaInfo[] metaInfos = null;
            metaInfos = node.getMetaInfos();
            for (IMetaInfo metaInfo : metaInfos)
            {
                String name = metaInfo.getTagName();
                if (name.equals("Bindable")
                        && metaInfo.getAllAttributes().length == 0)
                {
                    isBindableSetter = true;
                    break;
                }
            }
        }
        if (isBindableSetter)
        {
            //write(ASEmitterTokens.FUNCTION);
            //emitParameters(node.getParameterNodes());
            write(ASEmitterTokens.SPACE);
            writeNewline(ASEmitterTokens.BLOCK_OPEN);

            write(ASEmitterTokens.VAR);
            write(ASEmitterTokens.SPACE);
            write("oldValue");
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.EQUAL);
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.THIS);
            write(ASEmitterTokens.MEMBER_ACCESS);
            write(node.getName());
            //write(ASEmitterTokens.PAREN_OPEN);
            //write(ASEmitterTokens.PAREN_CLOSE);
            writeNewline(ASEmitterTokens.SEMICOLON);

            // add change check
            write(ASEmitterTokens.IF);
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.PAREN_OPEN);
            write("oldValue");
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.STRICT_EQUAL);
            write(ASEmitterTokens.SPACE);
            IParameterNode[] params = node.getParameterNodes();
            write(params[0].getName());
            write(ASEmitterTokens.PAREN_CLOSE);
            write(ASEmitterTokens.SPACE);
            write(ASEmitterTokens.RETURN);
            writeNewline(ASEmitterTokens.SEMICOLON);

            write(ASEmitterTokens.THIS);
            write(ASEmitterTokens.MEMBER_ACCESS);
            write("__bindingWrappedSetter__" + node.getName());
            write(ASEmitterTokens.PAREN_OPEN);
            write(params[0].getName());
            write(ASEmitterTokens.PAREN_CLOSE);
            writeNewline(ASEmitterTokens.SEMICOLON);

            // add dispatch of change event
            writeNewline("    this.dispatchEvent(org_apache_flex_events_ValueChangeEvent.createUpdateEvent(");
            writeNewline("         this, \"" + node.getName()
                    + "\", oldValue, " + params[0].getName() + "));");
            write(ASEmitterTokens.BLOCK_CLOSE);
            //writeNewline(ASEmitterTokens.SEMICOLON);
            writeNewline();
            writeNewline();
        }
        else
            super.emitDefinePropertyFunction(node);
    }

    @Override
    public void emitPackageHeader(IPackageDefinition definition)
    {
        packageHeaderEmitter.emit(definition);
    }

    @Override
    public void emitPackageHeaderContents(IPackageDefinition definition)
    {
        packageHeaderEmitter.emitContents(definition);
    }

    @Override
    public void emitPackageFooter(IPackageDefinition definition)
    {
        packageFooterEmitter.emit(definition);
    }

    @Override
    public void emitForEachLoop(IForLoopNode node)
    {
        IBinaryOperatorNode bnode = (IBinaryOperatorNode) node
                .getConditionalsContainerNode().getChild(0);
        IASNode childNode = bnode.getChild(0);

        String iterName = "foreachiter"
                + new Integer(foreachLoopCounter).toString();
        foreachLoopCounter++;

        write(ASEmitterTokens.FOR);
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.PAREN_OPEN);
        write(ASEmitterTokens.VAR);
        write(ASEmitterTokens.SPACE);
        write(iterName);
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.IN);
        write(ASEmitterTokens.SPACE);
        getWalker().walk(bnode.getChild(1));
        writeToken(ASEmitterTokens.PAREN_CLOSE);
        writeNewline();
        write(ASEmitterTokens.BLOCK_OPEN);
        writeNewline();
        if (childNode instanceof IVariableExpressionNode)
        {
            write(ASEmitterTokens.VAR);
            write(ASEmitterTokens.SPACE);
            write(((IVariableNode) childNode.getChild(0)).getName());
        }
        else
            write(((IIdentifierNode) childNode).getName());
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.EQUAL);
        write(ASEmitterTokens.SPACE);
        getWalker().walk(bnode.getChild(1));
        write(ASEmitterTokens.SQUARE_OPEN);
        write(iterName);
        write(ASEmitterTokens.SQUARE_CLOSE);
        write(ASEmitterTokens.SEMICOLON);
        writeNewline();
        getWalker().walk(node.getStatementContentsNode());
        write(ASEmitterTokens.BLOCK_CLOSE);
        writeNewline();

    }

    /*
    @Override
    public void emitForEachLoop(IForLoopNode node)
    {
        IContainerNode xnode = (IContainerNode) node.getChild(1);
        IBinaryOperatorNode bnode = (IBinaryOperatorNode) node
                .getConditionalsContainerNode().getChild(0);
        IASNode childNode = bnode.getChild(0);

        write(ASEmitterTokens.TRY);
        write(ASEmitterTokens.BLOCK_OPEN);
        writeNewline();
        
        write(JSGoogEmitterTokens.GOOG_ARRAY_FOREACH);
        write(ASEmitterTokens.PAREN_OPEN);
        getWalker().walk(bnode.getChild(1));
        writeToken(ASEmitterTokens.COMMA);
        writeToken(ASEmitterTokens.FUNCTION);
        write(ASEmitterTokens.PAREN_OPEN);
        if (childNode instanceof IVariableExpressionNode)
        	write(((IVariableNode) childNode.getChild(0)).getName());
        else
        	write(((IIdentifierNode) childNode).getName());
        writeToken(ASEmitterTokens.PAREN_CLOSE);
        if (isImplicit(xnode))
            write(ASEmitterTokens.BLOCK_OPEN);
        getWalker().walk(node.getStatementContentsNode());
        if (isImplicit(xnode))
        {
            writeNewline();
            write(ASEmitterTokens.BLOCK_CLOSE);
        }
        write(ASEmitterTokens.PAREN_CLOSE);
        writeNewline();
        write(ASEmitterTokens.BLOCK_CLOSE);
        writeNewline();
        write(ASEmitterTokens.CATCH);
        write(ASEmitterTokens.PAREN_OPEN);
        write("foreachbreakerror");
        write(ASEmitterTokens.PAREN_CLOSE);
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.BLOCK_OPEN);
        write(ASEmitterTokens.BLOCK_CLOSE);
        writeNewline();
        
    }

    @Override
    public void emitIterationFlow(IIterationFlowNode node)
    {
    	// look for break in foreach and throw error instead
    	if (node.getKind() == IIterationFlowNode.IterationFlowKind.BREAK)
    	{
    		IASNode pNode = node.getParent();
    		while (pNode != null)
    		{
    			ASTNodeID id = pNode.getNodeID();
    			if (id == ASTNodeID.ForEachLoopID)
    			{
    				write(ASEmitterTokens.THROW);
    				write(ASEmitterTokens.SPACE);
    				write(ASEmitterTokens.NEW);
    				write(ASEmitterTokens.SPACE);
    				write(JSGoogEmitterTokens.ERROR);
    				write(ASEmitterTokens.PAREN_OPEN);
    				write(ASEmitterTokens.PAREN_CLOSE);
    				write(ASEmitterTokens.SEMICOLON);
    				return;
    			}
    			else if (id == ASTNodeID.ForLoopID ||
    					id == ASTNodeID.DoWhileLoopID ||
    					id == ASTNodeID.WhileLoopID)
    				break;
    			pNode = pNode.getParent();
    		}
    	}
        write(node.getKind().toString().toLowerCase());
        IIdentifierNode lnode = node.getLabelNode();
        if (lnode != null)
        {
            write(ASEmitterTokens.SPACE);
            getWalker().walk(lnode);
        }
    }
    */

    @Override
    public void emitTypedExpression(ITypedExpressionNode node)
    {
        write(JSGoogEmitterTokens.ARRAY);
    }

    @Override
    public void emitLiteral(ILiteralNode node)
    {
        boolean isWritten = false;

        String s = node.getValue(true);
        if (!(node instanceof RegExpLiteralNode))
        {
            if (node.getLiteralType() == LiteralType.XML)
            {
                // ToDo (erikdebruin): VF2JS -> handle XML output properly...

                write("'" + s + "'");

                isWritten = true;
            }
            s = s.replaceAll("\n", "__NEWLINE_PLACEHOLDER__");
            s = s.replaceAll("\r", "__CR_PLACEHOLDER__");
            s = s.replaceAll("\t", "__TAB_PLACEHOLDER__");
            s = s.replaceAll("\f", "__FORMFEED_PLACEHOLDER__");
            s = s.replaceAll("\b", "__BACKSPACE_PLACEHOLDER__");
            s = s.replaceAll("\\\\\"", "__QUOTE_PLACEHOLDER__");
            s = s.replaceAll("\\\\", "__ESCAPE_PLACEHOLDER__");
            //s = "\'" + s.replaceAll("\'", "\\\\\'") + "\'";
            s = s.replaceAll("__ESCAPE_PLACEHOLDER__", "\\\\\\\\");
            s = s.replaceAll("__QUOTE_PLACEHOLDER__", "\\\\\"");
            s = s.replaceAll("__BACKSPACE_PLACEHOLDER__", "\\\\b");
            s = s.replaceAll("__FORMFEED_PLACEHOLDER__", "\\\\f");
            s = s.replaceAll("__TAB_PLACEHOLDER__", "\\\\t");
            s = s.replaceAll("__CR_PLACEHOLDER__", "\\\\r");
            s = s.replaceAll("__NEWLINE_PLACEHOLDER__", "\\\\n");
        }

        if (!isWritten)
        {
            write(s);
        }
    }

    @Override
    public void emitE4XFilter(IMemberAccessExpressionNode node)
    {
        // ToDo (erikdebruin): implement E4X replacement !?!
        write(ASEmitterTokens.SINGLE_QUOTE);
        write("E4XFilter");
        write(ASEmitterTokens.SINGLE_QUOTE);
    }


    @Override
    public String formatQualifiedName(String name)
    {
        if (name.contains("goog.") || name.startsWith("Vector."))
            return name;
        name = name.replaceAll("\\.", "_");
        return name;
    }

}

/*
@Override
public void emitInterface(IInterfaceNode node)
{
    ICompilerProject project = getWalker().getProject();

    getDoc().emitInterfaceDoc(node, project);

    String qname = node.getQualifiedName();
    if (qname != null && !qname.equals(""))
    {
        write(formatQualifiedName(qname));
        write(ASEmitterTokens.SPACE);
        writeToken(ASEmitterTokens.EQUAL);
        write(ASEmitterTokens.FUNCTION);
        write(ASEmitterTokens.PAREN_OPEN);
        write(ASEmitterTokens.PAREN_CLOSE);
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.BLOCK_OPEN);
        writeNewline();
        write(ASEmitterTokens.BLOCK_CLOSE);
        write(ASEmitterTokens.SEMICOLON);
    }

    
    final IDefinitionNode[] members = node.getAllMemberDefinitionNodes();
    for (IDefinitionNode mnode : members)
    {
        boolean isAccessor = mnode.getNodeID() == ASTNodeID.GetterID
                || mnode.getNodeID() == ASTNodeID.SetterID;

        writeNewline();
        writeNewline();
        writeNewline();

        getDoc().emitInterfaceMemberDoc((IFunctionNode) mnode, project);
        
        write(formatQualifiedName(qname));
        write(ASEmitterTokens.MEMBER_ACCESS);
        write(JSEmitterTokens.PROTOTYPE);
        write(ASEmitterTokens.MEMBER_ACCESS);
        if (isAccessor)
        {
            writeGetSetPrefix(mnode.getNodeID() == ASTNodeID.GetterID);
        }
        write(mnode.getQualifiedName());
        write(ASEmitterTokens.SPACE);
        writeToken(ASEmitterTokens.EQUAL);
        write(ASEmitterTokens.FUNCTION);
        emitParameters(((IFunctionNode) mnode).getParameterNodes());
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.BLOCK_OPEN);
        write(ASEmitterTokens.BLOCK_CLOSE);
        write(ASEmitterTokens.SEMICOLON);
    }
}
*/

/*
@Override
public void emitMethod(IFunctionNode node)
{
    FunctionNode fn = (FunctionNode) node;
    fn.parseFunctionBody(getProblems());

    ICompilerProject project = getWalker().getProject();

    getDoc().emitMethodDoc(node, project);

    boolean isConstructor = node.isConstructor();

    String qname = getTypeDefinition(node).getQualifiedName();
    if (qname != null && !qname.equals(""))
    {
        write(formatQualifiedName(qname));
        if (!isConstructor)
        {
            write(ASEmitterTokens.MEMBER_ACCESS);
            if (!fn.hasModifier(ASModifier.STATIC))
            {
                write(JSEmitterTokens.PROTOTYPE);
                write(ASEmitterTokens.MEMBER_ACCESS);
            }
        }
    }

    if (!isConstructor)
        emitMemberName(node);

    write(ASEmitterTokens.SPACE);
    writeToken(ASEmitterTokens.EQUAL);
    write(ASEmitterTokens.FUNCTION);

    emitParameters(node.getParameterNodes());

    boolean hasSuperClass = hasSuperClass(node);

    if (isConstructor && node.getScopedNode().getChildCount() == 0)
    {
        write(ASEmitterTokens.SPACE);
        write(ASEmitterTokens.BLOCK_OPEN);
        if (hasSuperClass)
            emitSuperCall(node, CONSTRUCTOR_EMPTY);
        writeNewline();
        write(ASEmitterTokens.BLOCK_CLOSE);
    }

    if (!isConstructor || node.getScopedNode().getChildCount() > 0)
        emitMethodScope(node.getScopedNode());

    if (isConstructor && hasSuperClass)
    {
        writeNewline(ASEmitterTokens.SEMICOLON);
        write(JSGoogEmitterTokens.GOOG_INHERITS);
        write(ASEmitterTokens.PAREN_OPEN);
        write(formatQualifiedName(qname));
        writeToken(ASEmitterTokens.COMMA);
        String sname = getSuperClassDefinition(node, project)
                .getQualifiedName();
        write(formatQualifiedName(sname));
        write(ASEmitterTokens.PAREN_CLOSE);
    }
}
*/

/*
@Override
protected void emitDefaultParameterCodeBlock(IFunctionNode node)
{
    IParameterNode[] pnodes = node.getParameterNodes();
    if (pnodes.length == 0)
        return;

    Map<Integer, IParameterNode> defaults = getDefaults(pnodes);

    if (defaults != null)
    {
        final StringBuilder code = new StringBuilder();

        if (!hasBody(node))
        {
            indentPush();
            write(JSFlexJSEmitterTokens.INDENT);
        }

        List<IParameterNode> parameters = new ArrayList<IParameterNode>(
                defaults.values());

        for (int i = 0, n = parameters.size(); i < n; i++)
        {
            IParameterNode pnode = parameters.get(i);

            if (pnode != null)
            {
                code.setLength(0);

                // x = typeof y !== 'undefined' ? y : z;\n 
                code.append(pnode.getName());
                code.append(ASEmitterTokens.SPACE.getToken());
                code.append(ASEmitterTokens.EQUAL.getToken());
                code.append(ASEmitterTokens.SPACE.getToken());
                code.append(ASEmitterTokens.TYPEOF.getToken());
                code.append(ASEmitterTokens.SPACE.getToken());
                code.append(pnode.getName());
                code.append(ASEmitterTokens.SPACE.getToken());
                code.append(ASEmitterTokens.STRICT_NOT_EQUAL.getToken());
                code.append(ASEmitterTokens.SPACE.getToken());
                code.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
                code.append(ASEmitterTokens.UNDEFINED.getToken());
                code.append(ASEmitterTokens.SINGLE_QUOTE.getToken());
                code.append(ASEmitterTokens.SPACE.getToken());
                code.append(ASEmitterTokens.TERNARY.getToken());
                code.append(ASEmitterTokens.SPACE.getToken());
                code.append(pnode.getName());
                code.append(ASEmitterTokens.SPACE.getToken());
                code.append(ASEmitterTokens.COLON.getToken());
                code.append(ASEmitterTokens.SPACE.getToken());
                code.append(pnode.getDefaultValue());
                code.append(ASEmitterTokens.SEMICOLON.getToken());

                write(code.toString());

                if (i == n - 1 && !hasBody(node))
                    indentPop();

                writeNewline();
            }
        }
    }
}
*/

/*
private void writeGetSetPrefix(boolean isGet)
{
    if (isGet)
        write(ASEmitterTokens.GET);
    else
        write(ASEmitterTokens.SET);
    write("_");
}
*/

/*
@Override
public void emitUnaryOperator(IUnaryOperatorNode node)
{
    if (node.getNodeID() == ASTNodeID.Op_PreIncrID
            || node.getNodeID() == ASTNodeID.Op_PreDecrID
            || node.getNodeID() == ASTNodeID.Op_PostIncrID
            || node.getNodeID() == ASTNodeID.Op_PostDecrID)
    {
        IExpressionNode opNode = node.getOperandNode();
        String getString = stringifyNode(opNode);
        int index = getString.lastIndexOf("get_");
        if (index != -1)
        {
            write(JSFlexJSEmitterTokens.LANGUAGE_QNAME);
            write(ASEmitterTokens.MEMBER_ACCESS);
            if (node.getNodeID() == ASTNodeID.Op_PreIncrID)
                write(JSFlexJSEmitterTokens.PREINCREMENT);
            else if (node.getNodeID() == ASTNodeID.Op_PostIncrID)
                write(JSFlexJSEmitterTokens.POSTINCREMENT);
            else if (node.getNodeID() == ASTNodeID.Op_PreDecrID)
                write(JSFlexJSEmitterTokens.PREDECREMENT);
            else
                write(JSFlexJSEmitterTokens.POSTDECREMENT);
            write(ASEmitterTokens.PAREN_OPEN);
            String obj = getString.substring(0, index - 1);
            write(obj);
            write(ASEmitterTokens.COMMA);
            String prop = getString.substring(index + 4);               
            int endIndex = prop.indexOf(ASEmitterTokens.PAREN_OPEN.getToken());
            prop = prop.substring(0, endIndex);
            write(ASEmitterTokens.DOUBLE_QUOTE);
            write(prop);
            write(ASEmitterTokens.DOUBLE_QUOTE);
            write(ASEmitterTokens.PAREN_CLOSE);
            return;
        }
        else
        {
            IASNode parentNode = node.getParent();
            if (parentNode.getNodeID() == ASTNodeID.MemberAccessExpressionID &&
                    ((MemberAccessExpressionNode)parentNode).getLeftOperandNode() == node)
            {
                // GCC wanted parens around foo++.toString().  As in (foo++).toString();
                write(ASEmitterTokens.PAREN_OPEN);
                super.emitUnaryOperator(node);
                write(ASEmitterTokens.PAREN_CLOSE);
                return;
            }
        }

    }
    super.emitUnaryOperator(node);
}
*/