/**
 * Created by Rohit Sinha on 5/21/15.
 */

import scala.util.parsing.combinator.syntactical.StandardTokenParsers
import scala.util.parsing.combinator.PackratParsers


abstract class UclOperator
case class UclRelationOperator(value : String) extends UclOperator {
  override def toString = value
}
case class UclUnaryOperator(value: String) extends UclOperator {
  override def toString = value
}
case class UclExtractOperator(high: UclNumber, low: UclNumber) {
  override def toString = "[" + high.toString + ":" + low.toString + "]"
}
case class UclMapSelectOperator(index: List[UclExpr]) extends UclOperator {
  override def toString = "[" + index.tail.fold(index.head.toString)
    { (acc,i) => acc + "," + i.toString } + "]"
}
case class UclMapStoreOperator(index: List[UclExpr], value: UclExpr) extends UclOperator {
  override def toString = "[" + index.tail.fold(index.head.toString)
    { (acc,i) => acc + "," + i.toString } + " := " + value.toString + "]"
}
case class UclFuncAppOperator(args: List[UclExpr]) extends UclOperator {
  override def toString = "(" + args.tail.fold(args.head.toString)
    { (acc,i) => acc + "," + i.toString } + ")"
}

abstract class UclExpr
case class UclIdentifier(value: String) extends UclExpr {
  override def toString = "@" + value.toString
}
case class UclNumber(value: Int) extends UclExpr {
  override def toString = value.toString
}
case class UclBitVector(value: Int, width: Int) extends UclExpr {
  override def toString = value.toString + "bv" + width.toString
}
case class UclBoolean(value: Boolean) extends UclExpr {
  override def toString = value.toString
}
case class UclEquivalence(left: UclExpr, right: UclExpr) extends UclExpr {
  override def toString = "(" + left.toString + " <==> " + right.toString + ")"
}
case class UclImplication(left: UclExpr, right: UclExpr) extends UclExpr {
  override def toString = "(" + left.toString + " ==> " + right.toString + ")"
}
case class UclConjunction(left: UclExpr, right: UclExpr) extends UclExpr {
  override def toString = "(" + left.toString + " /\\ " + right.toString + ")"
}
case class UclDisjunction(left: UclExpr, right: UclExpr) extends UclExpr {
  override def toString = "(" + left.toString + " \\/ " + right.toString + ")"
}
case class UclRelationOperation(op: UclRelationOperator, left: UclExpr, right: UclExpr) extends UclExpr {
  override def toString = "(" + left.toString + " " + op.toString + " " + right.toString + ")"
}
case class UclConcatOperation(left: UclExpr, right: UclExpr) extends UclExpr {
  override def toString = "(" + left.toString + " ++ " + right.toString + ")"
}
case class UclAddOperation(left: UclExpr, right: UclExpr) extends UclExpr {
  override def toString = "(" + left.toString + " + " + right.toString + ")"
}
case class UclMulOperation(left: UclExpr, right: UclExpr) extends UclExpr {
  override def toString = "(" + left.toString + " * " + right.toString + ")"
}
case class UclUnaryOperation(op: UclUnaryOperator, e: UclExpr) extends UclExpr {
  override def toString = "(" + op.toString + " " + e.toString + ")"
}
case class UclMapSelectOperation(x: UclExpr, op: UclMapSelectOperator) extends UclExpr {
  override def toString = x.toString + op.toString
}
case class UclMapStoreOperation(e: UclExpr, op: UclMapStoreOperator) extends UclExpr {
  override def toString = e.toString + op.toString
}
case class UclExtractOperation(e: UclExpr, op: UclExtractOperator) extends UclExpr {
  override def toString = e.toString + op.toString
}
case class UclFuncAppOperation(id: UclIdentifier, op: UclFuncAppOperator) extends UclExpr {
  override def toString = id.toString + op.toString
}

case class UclLhs(id: UclIdentifier, op: Option[UclMapSelectOperator]) {
  override def toString = op match {
    case Some(ms) => id.toString + ms.toString
    case None => id.toString
  }
}

abstract class UclType
case class UclBoolType() extends UclType { override def toString = "bool" }
case class UclIntType() extends UclType { override def toString = "int" }
case class UclEnumType(ids: List[UclIdentifier]) extends UclType {
  override def toString = "enum {" + 
    ids.tail.foldLeft(ids.head.toString) {(acc,i) => acc + "," + i.toString} + "}"
}
case class UclRecordType(fields: List[(UclIdentifier,UclType)]) extends UclType {
  override def toString = "record {" + 
    fields.tail.foldLeft(fields.head.toString) {(acc,i) => acc + "," + i.toString} + "}"
}
//class UclBitvectorType extends UclType
case class UclMapType(inTypes: List[UclType], outType: UclType) extends UclType {
  override def toString = "[" + inTypes.tail.fold(inTypes.head.toString)
  { (acc,i) => acc + "," + i.toString } + "] " + outType
}
case class UclSynonymType(id: UclIdentifier) extends UclType {
  override def toString = id.toString
}

/** Statements **/
abstract class UclStatement
case class UclSkipStmt() extends UclStatement {
  override def toString = "skip;"
}
case class UclAssertStmt(e: UclExpr) extends UclStatement {
  override def toString = "assert " + e + ";"
}
case class UclAssumeStmt(e: UclExpr) extends UclStatement {
  override def toString = "assume " + e + ";"
}
case class UclHavocStmt(id: UclIdentifier) extends UclStatement {
  override def toString = "havoc " + id + ";"
}
case class UclAssignStmt(lhss: List[UclLhs], rhss: List[UclExpr]) extends UclStatement {
  override def toString = lhss.tail.foldLeft(lhss.head.toString) { (acc,i) => acc + "," + i } +
    " := " +rhss.tail.foldLeft(rhss.head.toString) { (acc,i) => acc + "," + i } + ";"
}
case class UclIfElseStmt(cond: UclExpr, ifblock: List[UclStatement], elseblock: List[UclStatement])
  extends UclStatement {
  override def toString = "if " + cond + " {\n" + ifblock + "\n} else {\n" + elseblock + "\n}"
}
case class UclForStmt(id: UclIdentifier, range: (UclNumber,UclNumber), body: List[UclStatement])
  extends UclStatement
{
  override def toString = "for " + id + " in range(" + range._1 +"," + range._2 + ") {\n" + body + "}"
}
case class UclCaseStmt(body: List[(UclExpr,List[UclStatement])]) extends UclStatement {
  override def toString = "case" +
    body.foldLeft("") { (acc,i) => acc + "\n" + i._1.toString + " : " + i._2.toString + "\n"} + "esac"
}
case class UclProcedureCallStmt(id: UclIdentifier, callLhss: List[UclLhs], args: List[UclExpr])
  extends UclStatement {
  override def toString = "call (" +
    callLhss.foldLeft("") { (acc,i) => acc + "," + i } + ") := " + id + "(" +
    args.foldLeft("") { (acc,i) => acc + "," + i } + ")"
}

case class UclLocalVarDecl(id: UclIdentifier, typ: UclType) {
  override def toString = "localvar " + id + ": " + typ + ";"
}

case class UclSignature(inParams: List[(UclIdentifier,UclType)], outParams: List[(UclIdentifier,UclType)]) {
  type T = (UclIdentifier,UclType)
  val printfn = {(a: T) => a._1.toString + ":" + a._2.toString}
  override def toString =
    "(" + inParams.tail.foldLeft(printfn(inParams.head)) { (acc, i) => acc + "," + printfn(i) } + ")" +
    " returns " + "(" + outParams.tail.foldLeft(printfn(outParams.head)) { (acc,i) => acc + "," + printfn(i) } + ")"
}

abstract class UclDecl
case class UclProcedureDecl(id: UclIdentifier, sig: UclSignature, 
    decls: List[UclLocalVarDecl], body: List[UclStatement]) extends UclDecl {
  override def toString = "procedure " + id + sig +
    " {\n" + body.foldLeft("") { case (acc,i) => acc + "\t" + i + "\n" } + "}"
}
case class UclTypeDecl(id: UclIdentifier, typ: UclType) extends UclDecl {
  override def toString = "type " + id + " = " + typ 
}
case class UclStateVarDecl(id: UclIdentifier, typ: UclType) extends UclDecl {
  override def toString = "var " + id + ": " + typ + ";"
}
case class UclInputVarDecl(id: UclIdentifier, typ: UclType) extends UclDecl {
  override def toString = "input " + id + ": " + typ + ";"
}
case class UclOutputVarDecl(id: UclIdentifier, typ: UclType) extends UclDecl {
  override def toString = "output " + id + ": " + typ + ";"
}

case class UclModule(id: UclIdentifier, decls: List[UclDecl]) {
  override def toString = "\nmodule " + id + "{\n" + 
  decls.foldLeft("") { case (acc,i) => acc + i + "\n" } + "}\n"
}

object UclidParser extends StandardTokenParsers with PackratParsers {
  lazy val OpAnd = "&&"
  lazy val OpOr = "||"
  lazy val OpAdd = "+"
  lazy val OpMul = "*"
  lazy val OpBiImpl = "<==>"
  lazy val OpImpl = "==>"
  lazy val OpLT = "<"
  lazy val OpGT = ">"
  lazy val OpLE = "<="
  lazy val OpGE = ">="
  lazy val OpEQ = "=="
  lazy val OpNE = "!="
  lazy val OpConcat = "++"
  lazy val OpNeg = "!"
  lazy val OpMinus = "-"
  lazy val KwProcedure = "procedure"
  lazy val KwBool = "bool"
  lazy val KwInt = "int"
  lazy val KwEnum = "enum"
  lazy val KwRecord = "record"
  lazy val KwReturns = "returns"
  lazy val KwAssume = "assume"
  lazy val KwAssert = "assert"
  lazy val KwHavoc = "havoc"
  lazy val KwVar = "var"
  lazy val KwLocalVar = "localvar"
  lazy val KwSkip = "skip"
  lazy val KwCall = "call"
  lazy val KwIf = "if"
  lazy val KwElse = "else"
  lazy val KwCase = "case"
  lazy val KwEsac = "esac"
  lazy val KwFor = "for"
  lazy val KwIn = "in"
  lazy val KwRange = "range"
  lazy val KwType = "type"
  lazy val KwInput = "input"
  lazy val KwOutput = "output"
  lazy val KwModule = "module"

  lexical.delimiters ++= List("(", ")", ",", "[", "]", ":=", ":", 
    "bv", "{", "}", ";", "=",
    OpAnd, OpOr, OpAdd, OpMul, OpBiImpl, OpImpl,
    OpLT, OpGT, OpLE, OpGE, OpEQ, OpNE, OpConcat, OpNeg, OpMinus)
  lexical.reserved += (OpAnd, OpOr, OpAdd, OpMul, OpBiImpl, OpImpl,
    OpLT, OpGT, OpLE, OpGE, OpEQ, OpNE, OpConcat, OpNeg, OpMinus,
    "false", "true", "bv", KwProcedure, KwBool, KwInt, KwReturns,
    KwAssume, KwAssert, KwVar, KwLocalVar, KwHavoc, KwCall, KwIf, KwElse,
    KwCase, KwEsac, KwFor, KwIn, KwRange, KwLocalVar, KwInput, KwOutput,
    KwModule, KwType, KwEnum, KwRecord, KwSkip)

  lazy val ast_binary: UclExpr ~ String ~ UclExpr => UclExpr = {
    case x ~ OpBiImpl ~ y => UclEquivalence(x, y)
    case x ~ OpImpl ~ y => UclImplication(x, y)
    case x ~ OpAnd ~ y => UclConjunction(x, y)
    case x ~ OpOr ~ y => UclDisjunction(x, y)
    case x ~ OpLT ~ y => UclRelationOperation(UclRelationOperator(OpLT), x, y)
    case x ~ OpGT ~ y => UclRelationOperation(UclRelationOperator(OpGT), x, y)
    case x ~ OpLE ~ y => UclRelationOperation(UclRelationOperator(OpLE), x, y)
    case x ~ OpGE ~ y => UclRelationOperation(UclRelationOperator(OpGE), x, y)
    case x ~ OpEQ ~ y => UclRelationOperation(UclRelationOperator(OpEQ), x, y)
    case x ~ OpNE ~ y => UclRelationOperation(UclRelationOperator(OpNE), x, y)
    case x ~ OpConcat ~ y => UclConcatOperation(x, y)
    case x ~ OpAdd ~ y => UclAddOperation(x, y)
    case x ~ OpMul ~ y => UclMulOperation(x, y)
  }

  lazy val ast_unary: String ~ UclExpr => UclExpr = {
    case OpNeg ~ x => UclUnaryOperation(UclUnaryOperator(OpNeg), x)
    case OpMinus ~ x => UclUnaryOperation(UclUnaryOperator(OpMinus), x)
  }

  lazy val ast_mapselect: UclExpr ~ List[UclExpr] => UclMapSelectOperator = {
    case e ~ es => UclMapSelectOperator(e :: es)
  }

  lazy val ast_mapstore: UclExpr ~ List[UclExpr] ~ UclExpr => UclMapStoreOperator = {
    case e ~ es ~ r => UclMapStoreOperator(e :: es, r)
  }

  lazy val RelOp: Parser[String] = OpGT | OpLT | OpEQ | OpNE | OpGE | OpLE
  lazy val UnOp: Parser[String] = OpNeg | OpMinus
  lazy val MapSelectOp: Parser[UclMapSelectOperator] =
    ("[" ~> Expr ~ rep("," ~> Expr) <~ "]") ^^ ast_mapselect
  lazy val MapStoreOp: Parser[UclMapStoreOperator] =
    ("[" ~> (Expr ~ rep("," ~> Expr) ~ (":=" ~> Expr)) <~ "]") ^^ ast_mapstore
  lazy val ExtractOp: Parser[UclExtractOperator] =
    ("[" ~> Number ~ ":" ~ Number <~ "]") ^^ { case x ~ ":" ~ y => UclExtractOperator(x, y) }
  lazy val FuncApp: Parser[UclFuncAppOperator] =
    ExprList ^^ { case es => UclFuncAppOperator(es) }
  lazy val Id: PackratParser[UclIdentifier] = ident ^^ {case i => UclIdentifier(i)}
  lazy val Bool: PackratParser[UclBoolean] =
    "false" ^^ { _ => UclBoolean(false) } | "true" ^^ { _ => UclBoolean(true) }
  lazy val Number: PackratParser[UclNumber] = numericLit ^^ { case i => UclNumber(i.toInt) }
  //lazy val Bitvector: PackratParser[UclBitVector] = (numericLit ~ "bv" ~ numericLit) ^^
  //  { case h ~ "bv" ~ l => UclBitVector(h.toInt, l.toInt) }
  /** E0 := E1 OpEquiv E0 | E1  **/
  lazy val E0: PackratParser[UclExpr] = E1 ~ OpBiImpl ~ E0 ^^ ast_binary | E1
  /** E1 := E2 OpImpl E1 | E2  **/
  lazy val E1: PackratParser[UclExpr] = E2 ~ OpImpl ~ E1 ^^ ast_binary | E2
  /** E2 := E3 OpAnd E2 | E3 OpOr E2 | E3 **/
  lazy val E2: PackratParser[UclExpr] = E3 ~ OpAnd ~ E2 ^^ ast_binary | E3 ~ OpOr ~ E2 ^^ ast_binary | E3
  /** E3 := E4 OpRel E3 | E4  **/
  lazy val E3: PackratParser[UclExpr] = E4 ~ RelOp ~ E4 ^^ ast_binary | E4
  /** E4 := E5 OpConcat E4 | E5 **/
  lazy val E4: PackratParser[UclExpr] = E5 ~ OpConcat ~ E4 ^^ ast_binary | E5
  /** E5 := E6 OpAdd E5 | E6 **/
  lazy val E5: PackratParser[UclExpr] = E6 ~ OpAdd ~ E5 ^^ ast_binary | E6
  /** E6 := E7 OpMul E6 | E7 **/
  lazy val E6: PackratParser[UclExpr] = E7 ~ OpMul ~ E6 ^^ ast_binary | E7
  /** E7 := UnOp E8 | E8 **/
  lazy val E7: PackratParser[UclExpr] = UnOp ~ E8 ^^ ast_unary | E8
  /** E8 := E9 MapOp | E9 **/
  lazy val E8: PackratParser[UclExpr] =
      E9 ~ MapSelectOp ^^ { case e ~ m => UclMapSelectOperation(e, m) } |
      E9 ~ MapStoreOp ^^ { case e ~ m => UclMapStoreOperation(e, m) } |
      E9 ~ ExtractOp ^^ { case e ~ m => UclExtractOperation(e, m) } |
      E9
  /** E9 := false | true | Number | Bitvector | Id FuncApplication | (Expr) **/
  lazy val E9: PackratParser[UclExpr] =
      Bool |
      Number |
      "(" ~> Expr <~ ")" |
      Id ~ FuncApp ^^ {case id ~ f => UclFuncAppOperation(id,f)} |
      Id
  /** Expr := E0 **/
  lazy val Expr: PackratParser[UclExpr] = E0
  lazy val ExprList: Parser[List[UclExpr]] =
    ("(" ~> Expr ~ rep("," ~> Expr) <~ ")") ^^ { case e ~ es => e::es } |
    "(" ~> ")" ^^ { case _ => List.empty[UclExpr] }

  /** Examples of allowed types are bool | int | [int,int,bool] int **/
  lazy val PrimitiveType : PackratParser[UclType] =
    KwBool ^^ {case _ => UclBoolType()} | KwInt ^^ {case _ => UclIntType()}
  lazy val EnumType : PackratParser[UclEnumType] =
    KwEnum ~> ("{" ~> Id) ~ rep("," ~> Id) <~ "}" ^^ { case id ~ ids => UclEnumType(id::ids) }
  lazy val RecordType : PackratParser[UclRecordType] =
    KwRecord ~> ("{" ~> IdType) ~ rep("," ~> IdType) <~ "}" ^^ { case id ~ ids => UclRecordType(id::ids) }
  lazy val MapType : PackratParser[UclMapType] =
    "[" ~> PrimitiveType ~ (rep ("," ~> PrimitiveType) <~ "]") ~ PrimitiveType ^^
      { case t ~ ts ~ rt => UclMapType(t :: ts, rt)}
  lazy val SynonymType : PackratParser[UclSynonymType] = Id ^^ { case id => UclSynonymType(id) }
  lazy val Type : PackratParser[UclType] = 
    PrimitiveType | MapType | EnumType | RecordType | SynonymType

  lazy val IdType : PackratParser[(UclIdentifier,UclType)] =
    Id ~ (":" ~> Type) ^^ { case id ~ typ => (id,typ)}

  lazy val IdTypeList : PackratParser[List[(UclIdentifier,UclType)]] =
    "(" ~> IdType ~ (rep ("," ~> IdType) <~ ")") ^^ { case t ~ ts =>  t :: ts} |
    "(" ~ ")" ^^ { case _~_ => List.empty[(UclIdentifier,UclType)] }

  lazy val Lhs : PackratParser[UclLhs] =
    Id ~ MapSelectOp ^^ { case id ~ op => UclLhs(id, Some(op)) } |
    Id ^^ { case id => UclLhs(id, None) }

  lazy val LhsList: PackratParser[List[UclLhs]] =
    ("(" ~> Lhs ~ rep("," ~> Lhs) <~ ")") ^^ { case l ~ ls => l::ls } |
    "(" ~> ")" ^^ { case _ => List.empty[UclLhs] }

  lazy val RangeExpr: PackratParser[(UclNumber,UclNumber)] =
    KwRange ~> ("(" ~> Number ~ ("," ~> Number) <~ ")") ^^ { case x ~ y => (x,y) }

  lazy val LocalVarDecl : PackratParser[UclLocalVarDecl] =
    KwLocalVar ~> IdType <~ ";" ^^ { case (id,typ) => UclLocalVarDecl(id,typ)}
    
  lazy val Statement: PackratParser[UclStatement] =
    KwSkip <~ ";" ^^ { case _ => UclSkipStmt() } |
    KwAssert ~> Expr <~ ";" ^^ { case e => UclAssertStmt(e) } |
    KwAssume ~> Expr <~ ";" ^^ { case e => UclAssumeStmt(e) } |
    KwHavoc ~> Id <~ ";" ^^ { case id => UclHavocStmt(id) } |
    Lhs ~ rep("," ~> Lhs) ~ ":=" ~ Expr ~ rep("," ~> Expr) <~ ";" ^^
      { case l ~ ls ~ ":=" ~ r ~ rs => UclAssignStmt(l::ls, r::rs) } |
    KwCall ~> LhsList ~ (":=" ~> Id) ~ ExprList <~ ";" ^^
      { case lhss ~ id ~ args => UclProcedureCallStmt(id, lhss, args) } |
    KwIf ~> Expr ~ BlockStatement ~ (KwElse ~> BlockStatement) ^^
      { case e ~ f ~ g => UclIfElseStmt(e,f,g)} |
    KwCase ~> rep(CaseBlockStmt) <~ KwEsac ^^ 
      { case i => UclCaseStmt(i) } |
    KwFor ~> (Id ~ (KwIn ~> RangeExpr) ~ BlockStatement) ^^
      { case id ~ r ~ body => UclForStmt(id, r, body) }
    
  lazy val CaseBlockStmt: PackratParser[(UclExpr, List[UclStatement])] = 
    Expr ~ (":" ~> BlockStatement) ^^ { case e ~ ss => (e,ss) }
  lazy val BlockStatement: PackratParser[List[UclStatement]] = "{" ~> rep (Statement) <~ "}"

  lazy val ProcedureDecl : PackratParser[UclProcedureDecl] =
    KwProcedure ~> Id ~ IdTypeList ~ (KwReturns ~> IdTypeList) ~ 
      ("{" ~> rep(LocalVarDecl)) ~ (rep(Statement) <~ "}") ^^ 
      { case id ~ args ~ outs ~ decls ~ body =>  
        UclProcedureDecl(id, UclSignature(args,outs), decls, body) } |
    KwProcedure ~> Id ~ IdTypeList ~ ("{" ~> rep(LocalVarDecl)) ~ (rep(Statement) <~ "}") ^^
      { case id ~ args ~ decls ~ body => 
        UclProcedureDecl(id, UclSignature(args, List.empty[(UclIdentifier,UclType)]), decls, body) }
  
  lazy val TypeDecl : PackratParser[UclTypeDecl] =
    KwType ~> Id ~ ("=" ~> Type) <~ ";" ^^ { case id ~ t => UclTypeDecl(id,t) }
    
  lazy val VarDecl : PackratParser[UclStateVarDecl] =
    KwVar ~> IdType <~ ";" ^^ { case (id,typ) => UclStateVarDecl(id,typ)}
    
  lazy val InputDecl : PackratParser[UclInputVarDecl] =
    KwInput ~> IdType <~ ";" ^^ { case (id,typ) => UclInputVarDecl(id,typ)}
    
  lazy val OutputDecl : PackratParser[UclOutputVarDecl] =
    KwOutput ~> IdType <~ ";" ^^ { case (id,typ) => UclOutputVarDecl(id,typ)}
    
  lazy val Module: PackratParser[UclModule] =
    KwModule ~> Id ~ ("{" ~> rep(TypeDecl | VarDecl | InputDecl | OutputDecl | ProcedureDecl) <~ "}") ^^
    { case id ~ decls => UclModule(id, decls) }
    
  def parseExpr(input: String): UclExpr = {
    val tokens = new PackratReader(new lexical.Scanner(input))
    phrase(Expr)(tokens) match {
      case Success(ast, _) => ast
      case e: NoSuccess => throw new IllegalArgumentException(e.toString)
    }
  }

  def parseModule(input: String): UclModule = {
    val tokens = new PackratReader(new lexical.Scanner(input))
    phrase(Module)(tokens) match {
      case Success(ast, _) => ast
      case e: NoSuccess => throw new IllegalArgumentException(e.toString)
    }
  }

}