import scala.meta._, Term.{Name => TermName}, Type.{Name => TypeName, _}, Aux._

class TypeSuite extends ParseSuite {
  test("T") {
    val TypeName("T") = tpe("T")
  }

  test("F[T]") {
    val Apply(TypeName("F"), TypeName("T") :: Nil) = tpe("F[T]")
  }

  test("F#T") {
    val Project(TypeName("F"), TypeName("T")) = tpe("F#T")
  }

  // test("A * B") {
  //   val ApplyInfix(TypeName("A"), TypeName("*"), TypeName("B")) = tpe("A * B")
  // }

  test("A \\/ B") {
    val ApplyInfix(TypeName("A"), TypeName("\\/"), TypeName("B")) = tpe("A \\/ B")
  }

  test("f.T") {
    val Select(TermName("f"), TypeName("T")) = tpe("f.T")
  }

  test("f.type") {
    val Singleton(TermName("f")) = tpe("f.type")
  }

  test("super.T") {
    val Select(Qual.Super(None, None), TypeName("T")) = tpe("super.T")
  }

  test("this.T") {
    val Select(Term.This(None), TypeName("T")) = tpe("this.T")
  }

  test("(A, B)") {
    val Tuple(TypeName("A") :: TypeName("B") :: Nil) = tpe("(A, B)")
  }

  test("(A, B) => C") {
    val Function(TypeName("A") :: TypeName("B") :: Nil, TypeName("C")) =
      tpe("(A, B) => C")
  }

  test("T @foo") {
    val Annotate(TypeName("T"), Mod.Annot(TypeName("foo"), Nil) :: Nil) =
      tpe("T @foo")
  }

  test("A with B") {
    val comp @ Compound(TypeName("A") :: TypeName("B") :: Nil, Nil) = tpe("A with B")
    assert(comp.hasRefinement == false)
  }

  test("A with B {}") {
    val comp @ Compound(TypeName("A") :: TypeName("B") :: Nil, Nil) = tpe("A with B {}")
    assert(comp.hasRefinement == true)
  }

  test("{}") {
    val comp @ Compound(Nil, Nil) = tpe("{}")
    assert(comp.hasRefinement == true)
  }

  test("A { def x: A; val y: B; type C }") {
    val Compound(TypeName("A") :: Nil,
                 Decl.Def(Nil, TermName("x"),
                          Nil, Nil, Nil, TypeName("Int")) ::
                 Decl.Val(Nil, List(TermName("y")), TypeName("B")) ::
                 Decl.Type(Nil, TypeName("C"), Nil, EmptyBounds()) :: Nil) =
      tpe("A { def x: Int; val y: B; type C }")
  }

  test("F[_ >: lo <: hi]") {
    val Apply(TypeName("F"),
              Placeholder(TypeBounds(TypeName("lo"),
                                     TypeName("hi"))) :: Nil) =
      tpe("F[_ >: lo <: hi]")
  }

  test("F[_ >: lo") {
    val Apply(TypeName("F"),
              Placeholder(TypeBounds(TypeName("lo"), TypeName("Any"))) :: Nil) =
      tpe("F[_ >: lo]")
  }

  test("F[_ <: hi]") {
    val Apply(TypeName("F"),
              Placeholder(TypeBounds(TypeName("Nothing"), TypeName("hi"))) :: Nil) =
      tpe("F[_ <: hi]")
  }

  test("F[_]") {
    val Apply(TypeName("F"), Placeholder(EmptyBounds()) :: Nil) =
      tpe("F[_]")
  }

  test("F[T] forSome { type T }") {
    val Existential(Apply(TypeName("F"), TypeName("T") :: Nil),
                    Decl.Type(Nil, TypeName("T"), Nil, EmptyBounds()) :: Nil) =
      tpe("F[T] forSome { type T }")
  }

  test("a.T forSome { val a: A }") {
    val Existential(Select(TermName("a"), TypeName("T")),
                    Decl.Val(Nil, TermName("a") :: Nil, TypeName("A")) :: Nil) =
      tpe("a.T forSome { val a: A }")
  }
}
