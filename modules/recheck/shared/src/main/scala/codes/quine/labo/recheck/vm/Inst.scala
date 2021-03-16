package codes.quine.labo.recheck.vm

import codes.quine.labo.recheck.data.IChar
import codes.quine.labo.recheck.data.UChar

/** Inst is an instruction. */
sealed abstract class Inst {

  private[this] var _id: Int = _

  /** An internal id. It is unique in a program. */
  def id: Int = _id

  /** Sets an internal id. */
  private[vm] def id_=(id: Int): Unit = {
    this._id = id
  }
}

object Inst {

  /** Branch is a base class for terminal instructions.
    * Such a type instruction is placed in a last of a basic block.
    */
  sealed abstract class Terminator extends Inst {

    /** A successor block's labels. */
    def successors: Set[Label]
  }

  /** `ok`: Terminates a matching with a succeeded. */
  case object Ok extends Terminator {
    def successors: Set[Label] = Set.empty
    override def toString: String = "ok"
  }

  /** `jmp`: Jumps to a `next` block. */
  final case class Jmp(next: Label) extends Terminator {
    def successors: Set[Label] = Set(next)
    override def toString: String = s"jmp $next"
  }

  /** `try`: Jumps to a `next` block at first. When it is failed, it jumps to a `fallback` block. */
  final case class Try(next: Label, fallback: Label) extends Terminator {
    def successors: Set[Label] = Set(next, fallback)
    override def toString: String = s"try $next $fallback"
  }

  /** `cmp`: Compares a `reg` value with its operands, and jumps to a block depending on the comparison result.
    * If `reg < n`, it jumps to a `lt` label, or if `n <= reg`, it jumps to a `ge` label.
    */
  final case class Cmp(reg: Reg, n: Int, lt: Label, ge: Label) extends Terminator {
    def successors: Set[Label] = Set(lt, ge)
    override def toString: String = s"cmp $reg $n $lt $ge"
  }

  /** `rollback`: Terminates a matching with a rollback. */
  case object Rollback extends Terminator {
    def successors: Set[Label] = Set.empty
    override def toString: String = "rollback"
  }

  /** `tx`: Marks as a transaction point, then jumps a `next` block.
    * When it is terminated with a rollback, it jumps to a `rollback` block,
    * and when it is failed, it jumps to a `fallback` block.
    * `rollback` and `fallback` blocks are optional. When each of them is `None`, it means failed on jumping.
    */
  final case class Tx(next: Label, rollback: Option[Label], fallback: Option[Label]) extends Terminator {
    def successors: Set[Label] = Set(Some(next), rollback, fallback).flatten
    override def toString: String = s"tx $next ${rollback.getOrElse("FAIL")} ${fallback.getOrElse("FAIL")}"
  }

  /** NonTerminator is a base class for non-terminal instructions.
    * They appear in a basic block except its last.
    */
  sealed abstract class NonTerminator extends Inst

  /** `push_canary`: Pushes current position to a stack for canary checking. */
  case object PushCanary extends NonTerminator {
    override def toString: String = "push_canary"
  }

  /** `check_canary`: Checks current position is advanced from a last saved one.
    * If it is not, a matching is failed.
    */
  case object CheckCanary extends NonTerminator {
    override def toString: String = "check_canary"
  }

  /** `reset`: Sets a `reg` value to `0`. */
  final case class Reset(reg: Reg) extends NonTerminator {
    override def toString: String = s"reset $reg"
  }

  /** `inc`: Increments a `reg` value. */
  final case class Inc(reg: Reg) extends NonTerminator {
    override def toString: String = s"inc $reg"
  }

  /** `assert`: Tests current position without advancing. */
  final case class Assert(kind: AssertKind) extends NonTerminator {
    override def toString: String = s"assert $kind"
  }

  /** `read`: Tests current position with advancing. */
  final case class Read(kind: ReadKind, loc: Option[(Int, Int)]) extends NonTerminator {
    override def toString: String = {
      val loc = this.loc match {
        case Some((start, end)) => s" ; $start-$end"
        case None               => ""
      }
      s"read $kind$loc"
    }
  }

  /** `read_back`: Tests current position with receding. */
  final case class ReadBack(kind: ReadKind, loc: Option[(Int, Int)]) extends NonTerminator {
    override def toString: String = {
      val loc = this.loc match {
        case Some((start, end)) => s" ; $start-$end"
        case None               => ""
      }
      s"read_back $kind$loc"
    }
  }

  /** `cap_begin`: Captures current position as a beginning of a capture `index`. */
  final case class CapBegin(index: Int) extends NonTerminator {
    override def toString: String = s"cap_begin $index"
  }

  /** `cap_end`: Captures current position as an ending of a capture `index`. */
  final case class CapEnd(index: Int) extends NonTerminator {
    override def toString: String = s"cap_end $index"
  }

  /** `cap-reset`: Resets captures between `from` and `to`. */
  final case class CapReset(from: Int, to: Int) extends NonTerminator {
    override def toString: String = s"cap_reset $from $to"
  }

  /** AssertKind is an operand of `assert` instruction. */
  sealed abstract class AssertKind

  object AssertKind {

    /** `\b` assertion. */
    case object WordBoundary extends AssertKind {
      override def toString: String = "word_boundary"
    }

    /** `\B` assertion. */
    case object WordBoundaryNot extends AssertKind {
      override def toString: String = "word_boundary_not"
    }

    /** `^` assertion with `m` flag. */
    case object LineBegin extends AssertKind {
      override def toString: String = "line_begin"
    }

    /** `$` assertion with `m` flag. */
    case object LineEnd extends AssertKind {
      override def toString: String = "line_end"
    }

    /** `^` assertion. */
    case object InputBegin extends AssertKind {
      override def toString: String = "input_begin"
    }

    /** `$` assertion. */
    case object InputEnd extends AssertKind {
      override def toString: String = "input_end"
    }
  }

  /** ReadKind is an operand of `read` instruction. */
  sealed abstract class ReadKind

  object ReadKind {

    /** `.` matching with `s` flag. */
    case object Any extends ReadKind {
      override def toString: String = "any"
    }

    /** `.` matching. */
    case object Dot extends ReadKind {
      override def toString: String = "dot"
    }

    /** An usual character matching. */
    final case class Char(c: UChar) extends ReadKind {
      override def toString: String = s"char '$c'"
    }

    /** A character class matching. */
    final case class Class(s: IChar) extends ReadKind {
      override def toString: String = s"class $s"
    }

    /** An inverted character class matching. */
    final case class ClassNot(s: IChar) extends ReadKind {
      override def toString: String = s"class_not $s"
    }

    /** A capture reference matching. */
    final case class Ref(index: Int) extends ReadKind {
      override def toString: String = s"ref $index"
    }
  }
}