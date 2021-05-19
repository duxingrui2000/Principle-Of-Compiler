import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author dxr
 * @Description 启动词法分析与语法分析
 * @Date 20:33 5.18
 * @Param
 * @return
 **/
public class Compile {
    private static List<Token> tokenList = new LinkedList<>();
    LexicalAnalysis lexicalAnalysis;
    SyntaxAnalysis syntaxAnalysis;
    public Compile(){
        lexicalAnalysis = new LexicalAnalysis(tokenList);
        syntaxAnalysis = new SyntaxAnalysis(tokenList);
    }
    
    public static void main(String[] args) throws IOException {
        String filePath = "E:\\Java-WorkSpace\\Compiles_Principles\\src\\test.txt";
        String outPath = "E:\\Java-WorkSpace\\Compiles_Principles\\src\\out.txt";
        Compile compiler = new Compile();
        compiler.lexicalAnalysis.lexicalAnalysis(filePath, outPath);
        compiler.syntaxAnalysis.syntax_analyse();
        compiler.syntaxAnalysis.showTree();
    }
}
