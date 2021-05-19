/**
 * @Author dxr
 * @Description 为了语法分析器比较方便，定义Token
 * @Date 12:29 5.18
 * @Param
 * @return
 **/
public class Token {
    //该token类型
    private final TokenType tokenType;
    //该token的具体内容
    private final String content;
    
    public Token(TokenType tokenType, String content){
        this.tokenType = tokenType;
        this.content = content;
    }
    public TokenType getTokenType() {
        return tokenType;
    }
    public boolean isRelationOperator(){
        return tokenType==TokenType.GEQ || tokenType==TokenType.LEQ
                || tokenType==TokenType.GT || tokenType==TokenType.LT
                || tokenType==TokenType.EQ || tokenType==TokenType.NEQ;
    }
    @Override
    public String toString() {
        return "Token{" +
                "tokenType=" + tokenType +
                ", content='" + content + '\'' +
                '}';
    }
    
    public enum TokenType{
        IF, ELSE, INT, RETURN, VOID, WHILE,
        //+ - * /
        PLUS, MINUS, STAR, SLASH,
        //  > < >= <= == != =
        GT, LT, GEQ, LEQ, EQ, NEQ, ASSIGN,
        // , ;
        COMMA, SEMI,
        // ( )
        LP, RP,
        // [ ]
        LB, RB,
        // { }
        LC, RC,
        //id号，数字
        ID, INT_NUM,
        //终止
        END
    }
}

