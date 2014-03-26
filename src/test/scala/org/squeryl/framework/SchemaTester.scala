package org.squeryl.framework

import org.scalatest.matchers.ShouldMatchers
import org.squeryl.{AbstractSession, SessionFactory, Session, Schema}

import org.squeryl.test.PrimitiveTypeModeForTests._
import org.scalatest._
import org.scalatest.events.{TestIgnored, Ordinal}

abstract class SchemaTester extends DbTestBase{

  def schema : Schema

  def prePopulate() = {}

  override def beforeAll(){
    super.beforeAll
    if(notIgnored){
      transaction{
         schema.drop
         schema.create
        try{
          prePopulate
        }catch{
          case e : Exception =>
            println(e.getMessage)
            println(e.getStackTraceString)
        }
      }
    }
  }

  override def afterAll(){
    super.afterAll
    if(notIgnored){
      transaction{
         schema.drop
      }
    }
  }
}

abstract class DbTestBase extends FunSuite with ShouldMatchers with BeforeAndAfterAll with BeforeAndAfterEach {

  def connectToDb : Option[() => AbstractSession]

  var notIgnored = true

  val ignoredTests : List[String] = Nil

  override def beforeAll(){
    super.beforeAll
    SessionFactory.concreteFactory = connectWrapper()
  }

  def connectWrapper() : Option[() => AbstractSession] = {
    val connector = connectToDb
    if(connector.isEmpty){
      notIgnored = false
      None
    }else{
      Some(connector.get)
    }
  }

  override def runTest(
    testName: String,
    args: Args): Status = {

    if(!notIgnored || ignoredTests.find(_ == testName).isDefined){
      //reporter(TestIgnored(new Ordinal(0), suiteName, Some(this.getClass.getName),testName))
      return FailedStatus
    }
    super.runTest(testName, args)
  }
}

