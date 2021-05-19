import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.List;

public class SyntaxAnalysis {
    private List<Token> tokenList;//词法分析出的Token结果存放在此链表
    public static int tokenIndex = 0;//指明当前遍历到tokenList哪个位置
    private SyntaxTreeNode syntaxTree;
    //一个创造Node的工厂
    private TreeNodeFactory factory = new TreeNodeFactory();
    
    //定义29个BNF状态，另外多添加了一个ARRAY
    enum SyntaxType {
        PROGRAM,
        DECLARATION_LIST,
        DECLARATION,
        VAR_DECLARATION,
        ARRAY,
        TYPE_SPECIFIER,
        FUN_DECLARATION,
        PARAMS,
        PARAM_LIST,
        PARAM,
        COMPOUND_STMT,
        LOCAL_DECLARATIONS,
        STATEMENT_LIST,
        STATEMENT,
        EXPRESSION_STMT,
        SELECTION_STMT,
        ITERATION_STMT,
        RETURN_STMT,
        EXPRESSION,
        VAR,
        SIMPLE_EXPRESSION,
        RELATION_OP,
        ADDITIVE_EXPRESSION,
        ADD_OP,
        TERM,
        MUL_OP,
        FACTOR,
        CALL,
        ARGS,
        ARG_LIST
    }
    
    public SyntaxAnalysis(List<Token> list) {
        tokenList = list;
    }
    
    public int getTokenIndex() {
        return tokenIndex;
    }
    
    /**
     * @return Token返回下标index位置的token
     * @Author dxr
     * @Description 获取当前token
     * @Date 15:30 5.18
     * @Param [index]
     **/
    public Token getNowToken() {
        return tokenList.get(tokenIndex);
    }
    
    /**
     * @return void
     * @Author dxr
     * @Description 有些向下递归失败需要回溯，必须保存递归前的index位置
     * @Date 15:27 5.18
     * @Param [originIndex]原始的index下标
     **/
    public void recoverIndex(int originIndex) {
        tokenIndex = originIndex;
    }
    
    /**
     * @return void
     * @Author dxr
     * @Description 匹配tokenList中的结束符END
     * @Date 15:32 5.18
     * @Param []
     **/
    public void matchEnd() throws EndException {
        Token nowToken = this.getNowToken();
        if (nowToken.getTokenType() == Token.TokenType.END) {
            throw new EndException();
        }
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description match成功意味着我们可以创建一个新的叶子结点
     * @Date 15:39 5.18
     * @Param [expectType]
     **/
    public SyntaxTreeNode match(Token.TokenType expectType) throws NotMatchException {
        Token nowToken = getNowToken();
        if (expectType == nowToken.getTokenType()) {
            tokenIndex++;
            return factory.creatLeafNode(nowToken);
        } else {
            //匹配失败
            throw new NotMatchException(nowToken, expectType);
        }
    }
    public void showTreeHelper(SyntaxTreeNode treeNode){
        if (treeNode==null) return;
        if (treeNode instanceof ElseSyntaxTreeNode){
            System.out.println(treeNode);
            showTreeHelper(treeNode.left);
            showTreeHelper(treeNode.right);
            showTreeHelper(((ElseSyntaxTreeNode) treeNode).elseStatement);
        } else {
            System.out.println(treeNode);
            showTreeHelper(treeNode.left);
            showTreeHelper(treeNode.right);
        }
    }
    public void showTree(){
        if (syntaxTree==null){
            return;
        }
        showTreeHelper(syntaxTree);
    }
    /**
     * @return void
     * @Author dxr
     * @Description 从program开始进行语法分析
     * @Date 14:24 5.18
     * @Param []
     **/
    public void syntax_analyse() {
        try {
            syntaxTree = program();
        } catch (NotMatchException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description program → declaration-list
     * @Date 15:55 5.18
     * @Param []
     **/
    private SyntaxTreeNode program() throws NotMatchException {
        SyntaxTreeNode programNode = new SyntaxTreeNode(SyntaxType.PROGRAM);
        try {
            programNode.left = declarationList();
            return programNode;
        } catch (NotMatchException e) {
            throw e;
        } catch (EndException e) {
            return null;
        }
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description declaration-list → declaration-list declaration | declaration
     * @Date 15:56 5.18
     * @Param []
     **/
    private SyntaxTreeNode declarationList() throws NotMatchException, EndException {
        SyntaxTreeNode declarationListNode = factory.creatTreeNode(SyntaxType.DECLARATION_LIST);
        matchEnd();
        declarationListNode.left = declaration();
        int temp = getTokenIndex();
        try {
            declarationListNode.right = declarationList();
        } catch (NotMatchException e) {
            recoverIndex(temp);
        } catch (EndException e) {
            return declarationListNode;
        }
        return declarationListNode;
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description declaration → var-declaration | fun-declaration
     * var-declaration → type-specifier ID ; | type-specifier ID [NUM];
     * fun-declaration → type-specifier ID (params) compound-stmt
     * @Date 15:56 5.18
     * @Param []
     **/
    SyntaxTreeNode declaration() throws NotMatchException {
        SyntaxTreeNode declarationNode = factory.creatTreeNode(SyntaxType.DECLARATION);
        SyntaxTreeNode specific;
        int temp = getTokenIndex();
        try {
            specific = varDeclaration();
        } catch (NotMatchException e) {
            recoverIndex(temp);
            try {
                specific = funDeclaration();
            } catch (NotMatchException ex) {
                System.out.println("遇到ID，既无法匹配变量也无法匹配函数！");
                throw ex;
            }
//            specific = funDeclaration();
        }
        declarationNode.left = specific;
        return declarationNode;
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description var-declaration → type-specifier ID ; | type-specifier ID [NUM];
     * type-specifier → int | void
     * @Date 16:07 5.18
     * @Param []
     **/
    SyntaxTreeNode varDeclaration() throws NotMatchException {
        SyntaxTreeNode varNode = factory.creatTreeNode(SyntaxType.VAR_DECLARATION);
        int temp = getTokenIndex();
        try {
            varNode.left = typeSpecifier();
            SyntaxTreeNode IDNode = match(Token.TokenType.ID);
            int temp2 = getTokenIndex();
            try {
                SyntaxTreeNode arrayNode = arrayLength();
                arrayNode.left = IDNode;
                varNode.right = arrayNode;
            } catch (NotMatchException e) {
                varNode.right = IDNode;
                recoverIndex(temp2);
            }
            match(Token.TokenType.SEMI);
            return varNode;
        } catch (NotMatchException e) {
            recoverIndex(temp);
            throw e;
        }
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description 匹配可能出现的数组【ID】
     * var-declaration → type-specifier ID ; | type-specifier ID [NUM];
     * @Date 16:21 5.18
     * @Param []
     **/
    SyntaxTreeNode arrayLength() throws NotMatchException {
        SyntaxTreeNode arrayNode = factory.creatTreeNode(SyntaxType.ARRAY);
        SyntaxTreeNode numNode;
        int temp = getTokenIndex();
        try {
            match(Token.TokenType.LB);
            numNode = match(Token.TokenType.INT_NUM);
            match(Token.TokenType.RB);
            arrayNode.right = numNode;
            return arrayNode;
        } catch (NotMatchException e) {
            recoverIndex(temp);
            throw e;
        }
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description type-specifier → int | void
     * @Date 16:24 5.18
     * @Param []
     **/
    SyntaxTreeNode typeSpecifier() throws NotMatchException {
        SyntaxTreeNode typeSpecifierNode = factory.creatTreeNode(SyntaxType.TYPE_SPECIFIER);
        SyntaxTreeNode leaf_INT_OR_VOID;
        try {
            leaf_INT_OR_VOID = match(Token.TokenType.INT);
        } catch (NotMatchException e) {
            leaf_INT_OR_VOID = match(Token.TokenType.VOID);
        }
        typeSpecifierNode.left = leaf_INT_OR_VOID;
        return typeSpecifierNode;
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description fun-declaration → type-specifier ID (params) compound-stmt
     * @Date 16:28 5.18
     * @Param []
     **/
    SyntaxTreeNode funDeclaration() throws NotMatchException {
        int temp = getTokenIndex();
        SyntaxTreeNode funNode = factory.creatTreeNode(SyntaxType.FUN_DECLARATION);
        try {
            //funNode的左子树为函数类型
            funNode.left = typeSpecifier();
            //匹配函数名
            SyntaxTreeNode IDNode = match(Token.TokenType.ID);
            //匹配左括号
            match(Token.TokenType.LP);
            //ID的右子树（兄弟一串）就是参数params
            IDNode.setBrother(params());
            //匹配右括号
            match(Token.TokenType.RP);
            //参数params的右子树就是函数体compound_statement
            IDNode.right.setBrother(compoundStatement());
            //funNode的右子树为函数名字
            funNode.right = IDNode;
            return funNode;
        } catch (NotMatchException e) {
            recoverIndex(temp);
            throw e;
        }
    }
    
    /**
     * @return
     * @Author dxr
     * @Description params → param-list | void
     * param-list→ param-list , param | param
     * @Date 16:41 5.18
     * @Param
     **/
    SyntaxTreeNode params() throws NotMatchException {
        SyntaxTreeNode paramsNode = factory.creatTreeNode(SyntaxType.PARAMS);
        try {
            //直接是空参数void
            paramsNode.left = match(Token.TokenType.VOID);
            return paramsNode;
        } catch (NotMatchException e) {
            int temp = getTokenIndex();
            try {
                paramsNode.left = paramList();
                return paramsNode;
            } catch (NotMatchException ex) {
                recoverIndex(temp);
                throw ex;
            }
        }
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description param-list→ param-list , param | param
     * param → type-specifier ID | type-specifier ID [ ]
     * @Date 16:45 5.18
     * @Param []
     **/
    SyntaxTreeNode paramList() throws NotMatchException {
        SyntaxTreeNode paramListNode = factory.creatTreeNode(SyntaxType.PARAM_LIST);
        int temp = getTokenIndex();
        try {
            //匹配掉第一个参数
            paramListNode.left = param();
            //再确认是否有逗号，有则代表还有参数继续
            int temp2 = getTokenIndex();
            try {
                match(Token.TokenType.COMMA);
                paramListNode.right = paramList();
            } catch (NotMatchException e) {
                //未匹配到逗号则本轮函数的参数匹配结束
                //或者是匹配到逗号但是无法继续匹配paramList，恢复逗号之前的token，交给上层处理
                recoverIndex(temp2);
            }
            return paramListNode;
        } catch (NotMatchException e) {
            recoverIndex(temp);
            throw e;
        }
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description param → type-specifier ID | type-specifier ID [ ]
     * @Date 16:54 5.18
     * @Param []
     **/
    SyntaxTreeNode param() throws NotMatchException {
        int temp = getTokenIndex();
        SyntaxTreeNode paramNode = factory.creatTreeNode(SyntaxType.PARAM);
        try {
            //匹配掉变量类型
            paramNode.left = typeSpecifier();
            //接着匹配变量名字
            SyntaxTreeNode IDNode = match(Token.TokenType.ID);
            paramNode.right = IDNode;
            //确认是否是数组变量
            int temp2 = getTokenIndex();
            try {
                //有左右中括号说明是数组类型变量
                match(Token.TokenType.LB);
                match(Token.TokenType.RB);
                SyntaxTreeNode arrayNode = factory.creatTreeNode(SyntaxType.ARRAY);
                arrayNode.left = IDNode;
                paramNode.right = arrayNode;
            } catch (NotMatchException e) {
                //说明参数不是数组类型，退回到原来的index
                recoverIndex(temp2);
            }
            return paramNode;
        } catch (NotMatchException e) {
            //匹配typespecifier就失败了，退回到上层调用
            recoverIndex(temp);
            throw e;
        }
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description compound-stmt → { local-declarations statement-list }
     * local-declarations → local-declarations var-declaration | empty
     * var-declaration → type-specifier ID ; | type-specifier ID [NUM];
     * @Date 17:10 5.18
     * @Param []
     **/
    SyntaxTreeNode compoundStatement() throws NotMatchException {
        int temp = getTokenIndex();
        SyntaxTreeNode compoundNode = factory.creatTreeNode(SyntaxType.COMPOUND_STMT);
        try {
            //匹配掉大括号
            match(Token.TokenType.LC);
            compoundNode.left = localDeclarations();
            compoundNode.right = statementList();
            //匹配掉右大括号结束
            match(Token.TokenType.RC);
            return compoundNode;
        } catch (NotMatchException e) {
            recoverIndex(temp);
            throw e;
        }
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description local-declarations → local-declarations var-declaration | empty
     * 注意：这里出现了empty，也就是支持局部变量为空
     * @Date 17:14 5.18
     * @Param []
     **/
    SyntaxTreeNode localDeclarations() {
        SyntaxTreeNode localNode = factory.creatTreeNode(SyntaxType.LOCAL_DECLARATIONS);
        int temp = getTokenIndex();
        try {
            localNode.left = varDeclaration();
        } catch (NotMatchException e) {
            // 局部变量为空
            recoverIndex(temp);
            return null;
        }
        localNode.right = localDeclarations();
        return localNode;
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description statement-list → statement-list statement | empty
     * statement → expression-stmt | compound-stmt | selection-stmt | iteration-stmt | return-stmt
     * @Date 17:17 5.18
     * @Param []
     **/
    SyntaxTreeNode statementList() throws NotMatchException {
        SyntaxTreeNode statementListNode = factory.creatTreeNode(SyntaxType.STATEMENT_LIST);
        int temp = getTokenIndex();
        try {
            statementListNode.left = statement();
            statementListNode.right = statementList();
        } catch (NotMatchException e) {
            recoverIndex(temp);
        }
        return statementListNode;
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description statement → expression-stmt | compound-stmt | selection-stmt | iteration-stmt | return-stmt
     * @Date 17:20 5.18
     * @Param []
     **/
    SyntaxTreeNode statement() throws NotMatchException {
        SyntaxTreeNode statementNode = factory.creatTreeNode(SyntaxType.STATEMENT);
        Token nowToken = getNowToken();
        switch (nowToken.getTokenType()) {
            case IF:
                statementNode.left = selectionStatement();
                break;
            case WHILE:
                statementNode.left = iterationStatement();
                break;
            
            case RETURN:
                statementNode.left = returnStatement();
                break;
            case LC:
                statementNode.left = compoundStatement();
                break;
            default:
                statementNode.left = expressionStatement();
        }
        return statementNode;
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description expression-stmt → expression ; | ;
     * @Date 17:29 5.18
     * @Param []
     **/
    SyntaxTreeNode expressionStatement() throws NotMatchException {
        int temp = getTokenIndex();
        SyntaxTreeNode expressionStatementNode = factory.creatTreeNode(SyntaxType.EXPRESSION_STMT);
        try {
            int temp2 = getTokenIndex();
            try {
                expressionStatementNode.left = expression();
            } catch (NotMatchException e) {
                recoverIndex(temp2);
            }
            match(Token.TokenType.SEMI);
            return expressionStatementNode;
        } catch (NotMatchException e) {
            recoverIndex(temp);
            throw e;
        }
    }
    
    /**
     * @return ElseSyntaxTreeNode不同与其它返回的是SyntaxTreeNode，该函数返回的是SyntaxTreeNode的子类ElseSyntaxTreeNode
     * @Author dxr
     * @Description selection-stmt → if (expression) statement | if (expression) statement else statement
     * @Date 18:49 5.18
     * @Param []
     **/
    ElseSyntaxTreeNode selectionStatement() throws NotMatchException {
        ElseSyntaxTreeNode elseSyntaxTreeNode;
        int temp = getTokenIndex();
        try {
            //if(expression)statement处理完后
            match(Token.TokenType.IF);
            match(Token.TokenType.LP);
            SyntaxTreeNode expression = expression();
            match(Token.TokenType.RP);
            SyntaxTreeNode statement = statement();
            //接着考虑是否存在else,如果存在那么继续找else的statement
            //否则退回到原来的token下标，把else抛给上层去进一步判断处理
            int temp2 = getTokenIndex();
            SyntaxTreeNode statement2 = null;
            try {
                match(Token.TokenType.ELSE);
                statement2 = statement();
            } catch (NotMatchException e) {
                recoverIndex(temp2);
            }
            //无论statement2是否为null，我们面对if else 均创建ElseSyntaxTreeNode结点
            //如果statement2不为null，那么这个else后的statement()就保留在了ElseSyntaxTreeNode结点的elseStatement变量上
            //此时看起来像三叉树
            elseSyntaxTreeNode = factory.creatElseNode(statement2);
            elseSyntaxTreeNode.left = expression;
            elseSyntaxTreeNode.right = statement;
            
            return elseSyntaxTreeNode;
        } catch (NotMatchException e) {
            recoverIndex(temp);
            throw e;
        }
    }
    
    /**
     * @return
     * @Author dxr
     * @Description iteration-stmt → while ( expression ) statement
     * @Date 18:50 5.18
     * @Param
     **/
    SyntaxTreeNode iterationStatement() throws NotMatchException {
        int temp = getTokenIndex();
        SyntaxTreeNode iterationNode = factory.creatTreeNode(SyntaxType.ITERATION_STMT);
        try {
            match(Token.TokenType.WHILE);
            match(Token.TokenType.LP);
            iterationNode.left = expression();
            match(Token.TokenType.RP);
            iterationNode.right = statement();
            return iterationNode;
        } catch (NotMatchException e) {
            recoverIndex(temp);
            throw e;
        }
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description return-stmt → return; | return expression;
     * @Date 18:51 5.18
     * @Param []
     **/
    SyntaxTreeNode returnStatement() throws NotMatchException {
        SyntaxTreeNode returnNode = factory.creatTreeNode(SyntaxType.RETURN_STMT);
        int temp = getTokenIndex();
        try {
            returnNode.left = match(Token.TokenType.RETURN);
            //去找return后有没有expression
            int temp2 = getTokenIndex();
            try {
                returnNode.right = expression();
            } catch (Exception e) {
                recoverIndex(temp2);
            }
            match(Token.TokenType.SEMI);
            return returnNode;
        } catch (NotMatchException e) {
            recoverIndex(temp);
            throw e;
        }
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description expression → var = expression | simple-expression
     * @Date 18:58 5.18
     * @Param []
     **/
    SyntaxTreeNode expression() throws NotMatchException {
        SyntaxTreeNode expressionNode = factory.creatTreeNode(SyntaxType.EXPRESSION);
        int temp = getTokenIndex();
        try {
            //如果var=匹配成功了
            expressionNode.left = var();
            match(Token.TokenType.ASSIGN);
            expressionNode.right = expression();
            return expressionNode;
        } catch (NotMatchException e) {
            recoverIndex(temp);
            expressionNode.left = simpleExpression();
            return expressionNode;
        }
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description var → ID | ID [ expression ]
     * @Date 19:01 5.18
     * @Param []
     **/
    SyntaxTreeNode var() throws NotMatchException {
        SyntaxTreeNode varNode = factory.creatTreeNode(SyntaxType.VAR);
        int temp = getTokenIndex();
        try {
            varNode.left = match(Token.TokenType.ID);
            //不确定是否后面是否有中括号，还需要进一步判断
            int temp2 = getTokenIndex();
            //判断前保留好当前index的状态
            try {
                match(Token.TokenType.LB);
                SyntaxTreeNode expressionNode = expression();
                match(Token.TokenType.RB);
                
                varNode.right = expressionNode;
            } catch (NotMatchException e) {
                recoverIndex(temp2);
            }
            return varNode;
        } catch (NotMatchException e) {
            recoverIndex(temp);
            throw e;
        }
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description simple-expression → additive-expression relop additive-expression | additive-expression
     * additive-expression → additive-expression addop term | term
     * addop → + | -
     * term → term mulop factor | factor
     * @Date 19:09 5.18
     * @Param []
     **/
    SyntaxTreeNode simpleExpression() throws NotMatchException {
        SyntaxTreeNode simpleNode = factory.creatTreeNode(SyntaxType.SIMPLE_EXPRESSION);
        //如果additiveExpression有异常则直接抛给上层
        SyntaxTreeNode additiveNode = additiveExpression();
        //无异常继续判断 relop
        int temp = getTokenIndex();
        try {
            SyntaxTreeNode relationNode = relationOperation();
            relationNode.left = additiveNode;
            relationNode.right = additiveExpression();
            simpleNode.left = relationNode;
            return simpleNode;
        } catch (NotMatchException e) {
            recoverIndex(temp);
        }
        simpleNode.left = additiveNode;
        return simpleNode;
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description relop → <= | < | > | >= | == | !=
     * @Date 19:23 5.18
     * @Param []
     **/
    SyntaxTreeNode relationOperation() throws NotMatchException {
        Token nowToken = getNowToken();
        if (nowToken.isRelationOperator()) {
            //如果是六个操作符之一
            SyntaxTreeNode relation_Op_Node = factory.creatTreeNode(SyntaxType.RELATION_OP);
            relation_Op_Node.left = factory.creatLeafNode(nowToken);
            tokenIndex++;
            return relation_Op_Node;
        } else {
            throw new NotMatchException("遇到错误的操作符" + nowToken + " ,当前index=" + tokenIndex);
        }
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description additive-expression → additive-expression addop term | term
     * term → term mulop factor | factor
     * factor → ( expression ) | var | call | NUM
     * call → ID ( args )
     * var → ID | ID [ expression ]
     * @Date 19:35 5.18
     * @Param []
     **/
    SyntaxTreeNode additiveExpression() throws NotMatchException {
        SyntaxTreeNode additiveNode = factory.creatTreeNode(SyntaxType.ADDITIVE_EXPRESSION);
        //term有异常直接抛到上层去了
        SyntaxTreeNode termNode = term();
        //继续判断是否有addOp存在
        int temp = getTokenIndex();
        try {
            SyntaxTreeNode addOpNode = addOperation();
            addOpNode.left = termNode;
            addOpNode.right = additiveExpression();
            additiveNode.left = addOpNode;
            //有addOp存在
            return additiveNode;
        } catch (NotMatchException e) {
            recoverIndex(temp);
            additiveNode.left = termNode;
        }
        //没有addOp
        return additiveNode;
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description addop → + | -
     * @Date 19:48 5.18
     * @Param []
     **/
    SyntaxTreeNode addOperation() throws NotMatchException {
        SyntaxTreeNode addOperation = factory.creatTreeNode(SyntaxType.ADD_OP);
        try {
            addOperation.left = match(Token.TokenType.PLUS);
            return addOperation;
        } catch (NotMatchException e) {
            addOperation.left = match(Token.TokenType.MINUS);
            return addOperation;
        }
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description term → term mulop factor | factor
     * factor → ( expression ) | var | call | NUM
     * @Date 19:49 5.18
     * @Param []
     **/
    SyntaxTreeNode term() throws NotMatchException {
        SyntaxTreeNode termNode = factory.creatTreeNode(SyntaxType.TERM);
        SyntaxTreeNode factorNode = factor();
        //再去判断mulop是否存在
        int temp = getTokenIndex();
        try {
            SyntaxTreeNode mulopNode = multiplyOperation();
            mulopNode.left = factorNode;
            mulopNode.right = term();
            termNode.left = mulopNode;
            return termNode;
        } catch (NotMatchException e) {
            recoverIndex(temp);
        }
        termNode.left = factorNode;
        return termNode;
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description mulop → * | /
     * @Date 19:54 5.18
     * @Param []
     **/
    SyntaxTreeNode multiplyOperation() throws NotMatchException {
        SyntaxTreeNode multiplyNode = factory.creatTreeNode(SyntaxType.MUL_OP);
        try {
            multiplyNode.left = match(Token.TokenType.STAR);
        } catch (NotMatchException e) {
            multiplyNode.left = match(Token.TokenType.SLASH);
        }
        return multiplyNode;
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description factor → ( expression ) | var | call | NUM
     * @Date 19:56 5.18
     * @Param []
     **/
    SyntaxTreeNode factor() throws NotMatchException {
        SyntaxTreeNode factorNode = factory.creatTreeNode(SyntaxType.FACTOR);
        Token nowToken = getNowToken();
        int temp = getTokenIndex();
        try {
            if (nowToken.getTokenType() == Token.TokenType.INT_NUM) {
                factorNode.left = match(Token.TokenType.INT_NUM);
            } else if (nowToken.getTokenType() == Token.TokenType.LP) {
                match(Token.TokenType.LP);
                factorNode.left = expression();
                match(Token.TokenType.RP);
            } else {
                int temp2 = getTokenIndex();
                try {
                    factorNode.left = call();
                } catch (NotMatchException e) {
                    recoverIndex(temp2);
                    factorNode.left = var();
                }
            }
            return factorNode;
        } catch (NotMatchException e) {
            recoverIndex(temp);
            throw e;//一定抛，term中要确定factor到底成功没有
        }
    }
    
    /**
     * @return
     * @Author dxr
     * @Description call → ID ( args )
     * @Date 20:04 5.18
     * @Param
     **/
    SyntaxTreeNode call() throws NotMatchException {
        int temp = getTokenIndex();
        SyntaxTreeNode callNode = factory.creatTreeNode(SyntaxType.CALL);
        try {
            callNode.left = match(Token.TokenType.ID);
            match(Token.TokenType.LP);
            callNode.right = args();
            match(Token.TokenType.RP);
            return callNode;
        } catch (NotMatchException e) {
            recoverIndex(temp);
            throw e;//call有异常也得抛，让上层factor去转到var判断
        }
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description args → arg-list | empty
     * @Date 20:08 5.18
     * @Param []
     **/
    SyntaxTreeNode args() {
        SyntaxTreeNode args = factory.creatTreeNode(SyntaxType.ARGS);
        int temp = getTokenIndex();
        try {
            args.left = argList();
        } catch (NotMatchException e) {
            recoverIndex(temp);
        }
        return args;
    }
    
    /**
     * @return SyntaxTreeNode
     * @Author dxr
     * @Description arg-list → arg-list , expression | expression
     * @Date 20:10 5.18
     * @Param []
     **/
    SyntaxTreeNode argList() throws NotMatchException {
        SyntaxTreeNode arglistNode = factory.creatTreeNode(SyntaxType.ARG_LIST);
        //这里expression有错会抛出异常，则arg_list也抛出异常
        SyntaxTreeNode expressionNode = expression();
        //正确的话再去判断有误逗号
        int temp = getTokenIndex();
        try {
            match(Token.TokenType.COMMA);
            arglistNode.left = expressionNode;
            arglistNode.right = argList();
            return arglistNode;
        } catch (NotMatchException e) {
            //逗号匹配失败回退到原来的index
            recoverIndex(temp);
        }
        arglistNode.left = expressionNode;
        return arglistNode;
    }
}

/**
 * @Author dxr
 * @Description 语法树的结点结构(本质上就是棵二叉树)
 * @Date 14:21 5.18
 * @Param
 * @return
 **/
class SyntaxTreeNode {
    SyntaxAnalysis.SyntaxType syntaxType = null;
    Token token = null;
    SyntaxTreeNode left = null;
    SyntaxTreeNode right = null;
    
    public SyntaxTreeNode(SyntaxAnalysis.SyntaxType syntaxType) {
        this.syntaxType = syntaxType;
    }
    
    public SyntaxTreeNode(Token token) {
        this.token = token;
    }
    
    public void setBrother(SyntaxTreeNode brother) {
        right = brother;
    }
    
    @Override
    public String toString() {
        return "SyntaxTreeNode{" +
                "syntaxType=" + syntaxType +
                ", token=" + token +
                '}';
    }
}

/**
 * @Author dxr
 * @Description 存在else分支的BNF语法selection-stmt → if (expression) statement | if (expression) statement else statement
 * @Date 14:07 5.18
 * @Param
 * @return
 **/
class ElseSyntaxTreeNode extends SyntaxTreeNode {
    SyntaxTreeNode elseStatement;
    
    public ElseSyntaxTreeNode(SyntaxTreeNode elseStatement) {
        super(SyntaxAnalysis.SyntaxType.SELECTION_STMT);
        this.elseStatement = elseStatement;
    }
}

/**
 * @Author dxr
 * @Description 创造Node工厂
 * @Date 14:21 5.18
 * @Param
 * @return
 **/
class TreeNodeFactory {
    //创造普通非叶子结点存储（29+ARRAY)
    SyntaxTreeNode creatTreeNode(SyntaxAnalysis.SyntaxType type) {
        return new SyntaxTreeNode(type);
    }
    
    //创造叶子结点存储Token
    SyntaxTreeNode creatLeafNode(Token token) {
        return new SyntaxTreeNode(token);
    }
    
    //创造特殊的带有else的语句的非叶子结点(SELECTION_STMT)
    ElseSyntaxTreeNode creatElseNode(SyntaxTreeNode elseStatement) {
        return new ElseSyntaxTreeNode(elseStatement);
    }
}

/**
 * @Author dxr
 * @Description 无法完成匹配的异常，抛给上层选择另一种路径，如果没有则一层层抛到最顶层program，
 * program捕获后继续抛给syntax_analyse，爆出错误
 * @Date 14:32 5.18
 * @Param
 * @return
 **/
class NotMatchException extends Exception {
    Token token;
    
    NotMatchException(String message) {
        super(message);
    }
    
    NotMatchException(Token currentToken, Token.TokenType expectToken) {
        super("发生无法匹配的Token错误->期待Token是" + expectToken + "  ,接受到的Token是" + currentToken + "     tokenIndex=" + SyntaxAnalysis.tokenIndex);
    }
}

/**
 * @Author dxr
 * @Description 碰到END终止符了，代表已经结束
 * @Date 15:05 5.18
 * @Param
 * @return
 **/
class EndException extends Exception {
    EndException() {
        super();
    }
}