import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class LexicalAnalysis {
    private List<Token> tokenList;//定义一个tokenlist
    private StringBuilder profile = new StringBuilder();//文件读取内容
    private StringBuilder token = new StringBuilder();//变量名token内容
    private int p_index = 0;
    private int syn = -1;//标志符，目的是去对应string数组去找token类型
    private static final HashMap<String, Token.TokenType> KeyWordMap = new HashMap<>();
    static {
        KeyWordMap.put("int", Token.TokenType.INT);
        KeyWordMap.put("if", Token.TokenType.IF);
        KeyWordMap.put("else", Token.TokenType.ELSE);
        KeyWordMap.put("return", Token.TokenType.RETURN);
        KeyWordMap.put("void", Token.TokenType.VOID);
        KeyWordMap.put("while", Token.TokenType.WHILE);
        
        KeyWordMap.put("+", Token.TokenType.PLUS);
        KeyWordMap.put("-", Token.TokenType.MINUS);
        KeyWordMap.put("*", Token.TokenType.STAR);
        KeyWordMap.put("/", Token.TokenType.SLASH);
        KeyWordMap.put(">", Token.TokenType.GT);
        KeyWordMap.put("<", Token.TokenType.LT);
        KeyWordMap.put(">=", Token.TokenType.GEQ);
        KeyWordMap.put("<=", Token.TokenType.LEQ);
        KeyWordMap.put("==", Token.TokenType.EQ);
        KeyWordMap.put("!=", Token.TokenType.NEQ);
        KeyWordMap.put("=", Token.TokenType.ASSIGN);
        
        KeyWordMap.put(",", Token.TokenType.COMMA);
        KeyWordMap.put(";", Token.TokenType.SEMI);
        
        KeyWordMap.put("(", Token.TokenType.LP);
        KeyWordMap.put(")", Token.TokenType.RP);
        KeyWordMap.put("[", Token.TokenType.LB);
        KeyWordMap.put("]", Token.TokenType.RB);
        KeyWordMap.put("{", Token.TokenType.LC);
        KeyWordMap.put("}", Token.TokenType.RC);
    }
    private static String[] reserveWord = {"if","else","int","return","void","while"};//共计6个
    private static String[] operatorOrDelimiter = {"+", "-", "*", "/", "<", "<=", ">", ">=", "=", "==",
            "!=", ";", ",","(", ")","[", "]", "{", "}"};//共计19个，7<=syn<=25
    private static String[] operatorAbbreviations = {"PLUS","MINUS","TIMES","OVER","LT","LEQ","GT","GEQ",
            "ASSIGN","EQ","NEQ","SEMI","COMMA", "LPAREN","RPAREN","LMBRACKET","RMBRACKET","LBBRACKET","RBBRACKET"};
    private static String[] operatorButMayRepeat = {"<", "<=", ">", ">=", "=", "==", "!", "!="};
    public LexicalAnalysis(List<Token> tokenList){
        this.tokenList = tokenList;
    }
    private boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }
    
    private boolean isLetter(char ch) {
        return ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_');
    }
    
    private int isOperatorOrDelimiter(char ch) {
        int index = 0;
        for (String string : operatorOrDelimiter) {
            if (string.equals(ch + "")) {
                return index;
            }
            index++;
        }
        return -1;
    }
    
    private boolean isOperatorButMayRepeat(char ch) {
        for (String string : operatorButMayRepeat) {
            if (string.equals(ch + "")) {
                return true;
            }
        }
        return false;
    }
    
    private int isReserve(String str) {
        int index = 0;
        for (String string : reserveWord) {
            if (string.equals(str)) {
                return index;
            }
            index++;
        }
        return -1;
    }
    
    private void filter(StringBuilder s, int len) {
        StringBuilder news = new StringBuilder();
        for (int i = 0; i < len; i++) {
            if (s.charAt(i) == '/' && s.charAt(i + 1) == '/') {
                while (i<len && s.charAt(i) != '\n'){
                    i++;
                }
            }
            if (s.charAt(i) == '/' && s.charAt(i + 1) == '*') {
                i += 2;
                while (i<len && s.charAt(i) != '*' && s.charAt(i + 1) != '/') {
                    i++;
                }
                i += 2;
            }
            if (s.charAt(i) != '\n' && s.charAt(i) != '\t' && s.charAt(i) != '\r') {
                news.append(s.charAt(i));
            }
        }
        s.delete(0,s.length());
        s.append(news);
    }
    
    private void scanner() {
        if (p_index >= profile.length()) {
            syn = 0;
            return;
        }
        //去除空格
        while (profile.charAt(p_index) == ' ') {
            p_index++;
        }
        token.delete(0, token.length());//清空token
        char begin = profile.charAt(p_index);
        //以字母开头
        if (isLetter(begin)) {
            token.append(begin);
            p_index++;
            //后面的数字与字母全读取
            while (isLetter(profile.charAt(p_index)) || isDigit(profile.charAt(p_index))) {
                token.append(profile.charAt(p_index++));
            }
            //判断是否为保留字
            syn = isReserve(token.toString());
            if (syn == -1) {
                syn = 100;
            }else{
                syn++;//使得从1开始
            }
        } else if (isDigit(begin)) {
            //以数字开头
            token.append(begin);
            p_index++;
            while (isDigit(profile.charAt(p_index))) {
                token.append(profile.charAt(p_index++));
            }
            syn = 99;
        } else if (!isOperatorButMayRepeat(begin) && isOperatorOrDelimiter(begin) != -1) {
            //判断是运算符或分隔符并且不在isOperatorButMayRepeat里面，否则要判断后面的下一位是什么才能做决定
            token.append(begin);
            p_index++;
            syn = isOperatorOrDelimiter(begin) + 7;
        } else if (begin == '<') {
            p_index++;
            if (profile.charAt(p_index) == '=') {//<=
                syn = 12;
            }else {//<
                p_index--;
                syn = 11;
            }
            ++p_index;
        } else if (begin == '>') {
            p_index++;
            if (profile.charAt(p_index) == '=') {//>=
                syn = 14;
            }else {//>
                p_index--;
                syn = 13;
            }
            ++p_index;
        } else if (begin == '=') {
            p_index++;
            if (profile.charAt(p_index) == '=') {//==
                syn = 16;
            } else {//=
                p_index--;
                syn = 15;
            }
            ++p_index;
        } else if (begin == '!') {
            p_index++;
            if (profile.charAt(p_index) == '=') {//!=
                syn = 17;
            } else {//错误字符感叹号
                syn = 0;
                System.out.println("wrong letter:" + begin);
                System.exit(0);
            }
            ++p_index;
        } else {//有错误字符
            System.out.println("wrong letter:" + begin);
            System.exit(0);
        }
    }
    public void showTokenList(){
        System.out.println("-------------------------------");
        System.out.println("打印token列表");
        this.tokenList.add(new Token(Token.TokenType.END, "END"));
        for (Token t : this.tokenList) {
            System.out.println(t);
        }
        System.out.println("-------------------------------");
    }
    
    public void lexicalAnalysis(String filePath, String outPath) throws IOException {
        File file = new File(filePath);
        File outFile = new File(outPath);
        if (file.exists() && file.isFile()) {
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new FileReader(file));
                String linetxt = null;
                while ((linetxt = bufferedReader.readLine()) != null) {
                    this.profile.append(linetxt).append('\n');
                }
                System.out.println("源程序为：\n" + this.profile.toString());
                this.filter(this.profile, this.profile.length());
                System.out.println("经过过滤处理(注释)后为：\n" + this.profile.toString());
                //===========================================================
                //-----------将分析结果打印并写入文件------------------
                if (!outFile.exists()) {
                    outFile.createNewFile();
                }
                FileWriter fileWriter = new FileWriter(outFile);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                System.out.println("开始token识别：");
                while (this.syn != 0) {
                    this.scanner();
                    int syn = this.syn;
                    if (syn == 100) {
                        //标识符
                        this.tokenList.add(new Token(Token.TokenType.ID, this.token.toString()));
                        System.out.println("< ID, name=：" + "\"" + this.token + "\"" + " >");
                        bufferedWriter.write("< ID, name=：" + "\"" + this.token + "\"" + " >\n");
                    } else if (syn == 99) {
                        //常数
                        this.tokenList.add(new Token(Token.TokenType.INT_NUM, this.token.toString()));
                        System.out.println("< NUM, val=：" + "\"" + this.token + "\"" + " >");
                        bufferedWriter.write("< NUM, val=：" + "\"" + this.token + "\"" + " >\n");
                    } else if (syn >= 1 && syn <= 6) {
                        //保留字
                        String token = reserveWord[syn-1];
                        this.tokenList.add(new Token(KeyWordMap.get(token), token));
                        System.out.println("< reserved word：" + "\"" + token + "\"" + " >");
                        bufferedWriter.write("< reserved word：" + "\"" + token + "\"" + " >\n");
                    } else if (syn >= 7 && syn <= 25) {
                        //运算符或界符
                        String token = operatorOrDelimiter[syn - 7];
                        this.tokenList.add(new Token(KeyWordMap.get(token), token));
                        System.out.println("< "+operatorAbbreviations[syn-7]+"：" + "\"" + token + "\"" + " >");
                        bufferedWriter.write("< "+operatorAbbreviations[syn-7]+"：" + "\"" + token + "\"" + " >\n");
                    }
                }
                System.out.println("token识别结束!\n");
                bufferedWriter.close();
            } catch (Exception e) {
                System.out.println("产生异常：" + e.getMessage());
            } finally {
                assert bufferedReader != null;
                bufferedReader.close();
            }
        }
        this.showTokenList();
    }
//    public static void main(String[] args) throws IOException {
//        LexicalAnalysis this = new LexicalAnalysis(new LinkedList<Token>());
//        String filePath = "E:\\IDEA-Workspace\\DataStructures\\src\\Compiles_Principles\\test.txt";
//        String outPath = "E:\\IDEA-Workspace\\DataStructures\\src\\Compiles_Principles\\out.txt";
//        File file = new File(filePath);
//        File outFile = new File(outPath);
//        if (file.exists() && file.isFile()) {
//            BufferedReader bufferedReader = null;
//            try {
//                bufferedReader = new BufferedReader(new FileReader(file));
//                String linetxt = null;
//                while ((linetxt = bufferedReader.readLine()) != null) {
//                    this.profile.append(linetxt).append('\n');
//                }
//                System.out.println("源程序为：\n" + this.profile.toString());
//                this.filter(this.profile, this.profile.length());
//                System.out.println("经过过滤处理(注释)后为：\n" + this.profile.toString());
//                //===========================================================
//                //-----------将分析结果打印并写入文件------------------
//                if (!outFile.exists()) {
//                    outFile.createNewFile();
//                }
//                FileWriter fileWriter = new FileWriter(outFile);
//                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
//                System.out.println("开始token识别：");
//                while (this.syn != 0) {
//                    this.scanner();
//                    int syn = this.syn;
//                    if (syn == 100) {
//                        //标识符
//                        this.tokenList.add(new Token(Token.TokenType.ID, this.token.toString()));
//                        System.out.println("< ID, name=：" + "\"" + this.token + "\"" + " >");
//                        bufferedWriter.write("< ID, name=：" + "\"" + this.token + "\"" + " >\n");
//                    } else if (syn == 99) {
//                        //常数
//                        this.tokenList.add(new Token(Token.TokenType.INT_NUM, this.token.toString()));
//                        System.out.println("< NUM, val=：" + "\"" + this.token + "\"" + " >");
//                        bufferedWriter.write("< NUM, val=：" + "\"" + this.token + "\"" + " >\n");
//                    } else if (syn >= 1 && syn <= 6) {
//                        //保留字
//                        String token = reserveWord[syn-1];
//                        this.tokenList.add(new Token(KeyWordMap.get(token), token));
//                        System.out.println("< reserved word：" + "\"" + token + "\"" + " >");
//                        bufferedWriter.write("< reserved word：" + "\"" + token + "\"" + " >\n");
//                    } else if (syn >= 7 && syn <= 25) {
//                        //运算符或界符
//                        String token = operatorOrDelimiter[syn - 7];
//                        this.tokenList.add(new Token(KeyWordMap.get(token), token));
//                        System.out.println("< "+operatorAbbreviations[syn-7]+"：" + "\"" + token + "\"" + " >");
//                        bufferedWriter.write("< "+operatorAbbreviations[syn-7]+"：" + "\"" + token + "\"" + " >\n");
//                    }
//                }
//                System.out.println("token识别结束!\n");
//                bufferedWriter.close();
//            } catch (Exception e) {
//                System.out.println("产生异常：" + e.getMessage());
//            } finally {
//                assert bufferedReader != null;
//                bufferedReader.close();
//            }
//        }
//        this.showTokenList();
//    }
}













