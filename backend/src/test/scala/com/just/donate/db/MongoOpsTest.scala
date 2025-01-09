package com.just.donate.db

import munit.CatsEffectSuite
import org.mockito.Mockito.*
import org.mongodb.scala.*

import scala.concurrent.Future

class MongoOpsTest extends CatsEffectSuite {

  import MongoOps.*

  test("ObservableOps.toIO should handle an empty Observable") {
    val mockObservable = mock(classOf[Observable[String]])

    when(mockObservable.collect()).thenReturn(mock(classOf[SingleObservable[Seq[String]]]))
    when(mockObservable.toFuture()).thenReturn(Future.successful(Seq.empty))

    val resultIO = mockObservable.toIO

    resultIO.assertEquals(Seq.empty)
  }

  test("ObservableOps.toIO should handle an Observable with a single element") {
    val mockObservable = mock(classOf[Observable[String]])

    when(mockObservable.collect()).thenReturn(mock(classOf[SingleObservable[Seq[String]]]))
    when(mockObservable.toFuture()).thenReturn(Future.successful(Seq("singleElement")))

    val resultIO = mockObservable.toIO

    resultIO.assertEquals(Seq("singleElement"))
  }

  test("ObservableOps.toIO should handle an Observable with multiple elements") {
    val mockObservable = mock(classOf[Observable[String]])

    val elements = Seq("element1", "element2", "element3")
    when(mockObservable.collect()).thenReturn(mock(classOf[SingleObservable[Seq[String]]]))
    when(mockObservable.toFuture()).thenReturn(Future.successful(elements))

    val resultIO = mockObservable.toIO

    resultIO.assertEquals(elements)
  }

  test("ObservableOps.toIO should handle an Observable that fails with an exception") {
    val mockObservable = mock(classOf[Observable[String]])
    val exception = new RuntimeException("Test exception")

    when(mockObservable.collect()).thenReturn(mock(classOf[SingleObservable[Seq[String]]]))
    when(mockObservable.toFuture()).thenReturn(Future.failed(exception))

    val resultIO = mockObservable.toIO

    resultIO.attempt.assertEquals(Left(exception))
  }

  test("ObservableOps.toIO should handle a slow Observable") {
    val mockObservable = mock(classOf[Observable[String]])
    val elements = Seq("slow", "observable")

    when(mockObservable.collect()).thenReturn(mock(classOf[SingleObservable[Seq[String]]]))
    when(mockObservable.toFuture()).thenReturn(Future {
      Thread.sleep(100)
      elements
    })

    val resultIO = mockObservable.toIO

    resultIO.assertEquals(elements)
  }

  test("ObservableOps.toIO should handle an Observable with null elements") {
    val mockObservable = mock(classOf[Observable[String]])

    when(mockObservable.collect()).thenReturn(mock(classOf[SingleObservable[Seq[String]]]))
    when(mockObservable.toFuture()).thenReturn(Future.successful(Seq(null)))

    val resultIO = mockObservable.toIO

    resultIO.assertEquals(Seq(null))
  }

  test("ObservableOps.toIO should handle an Observable with repeated elements") {
    val mockObservable = mock(classOf[Observable[String]])

    val repeatedElements = Seq("repeat", "repeat", "repeat")
    when(mockObservable.collect()).thenReturn(mock(classOf[SingleObservable[Seq[String]]]))
    when(mockObservable.toFuture()).thenReturn(Future.successful(repeatedElements))

    val resultIO = mockObservable.toIO

    resultIO.assertEquals(repeatedElements)
  }

  test("ObservableOps.toIO should handle an Observable with mixed element types") {
    val mockObservable = mock(classOf[Observable[Any]])

    val mixedElements = Seq("string", 42, null, true)
    when(mockObservable.collect()).thenReturn(mock(classOf[SingleObservable[Seq[String]]]))
    when(mockObservable.toFuture()).thenReturn(Future.successful(mixedElements))

    val resultIO = mockObservable.toIO

    resultIO.assertEquals(mixedElements)
  }

  test("ObservableOps.toIO should handle an Observable with no subscribers") {
    val mockObservable = mock(classOf[Observable[String]])

    when(mockObservable.collect()).thenReturn(mock(classOf[SingleObservable[Seq[String]]]))
    when(mockObservable.toFuture()).thenReturn(Future.successful(Seq.empty))

    val resultIO = mockObservable.toIO

    resultIO.assertEquals(Seq.empty)
  }

  test("ObservableOps.toIO should handle an Observable with concurrent emissions") {
    val mockObservable = mock(classOf[Observable[String]])
    val elements = Seq("concurrent1", "concurrent2")

    when(mockObservable.collect()).thenReturn(mock(classOf[SingleObservable[Seq[String]]]))
    when(mockObservable.toFuture()).thenReturn(Future {
      elements.map(identity)
    })

    val resultIO = mockObservable.toIO

    resultIO.assertEquals(elements)
  }
}



