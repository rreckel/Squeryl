package org.squeryl.framework

import org.scalatest._
import org.squeryl.PrimitiveTypeMode.transaction
import org.squeryl.Session
import org.scalatest.Args

trait RunTestsInsideTransaction extends DbTestBase {

  override def runTest(
    testName: String,
    args: Args): Status = {
    if(!notIgnored){
      return super.runTest(testName, args)
    }

    // each test occur from within a transaction, that way when the test completes _all_ changes
    // made during the test are reverted so each test gets a clean enviroment to test against
    transaction {
      val status = super.runTest(testName, args)

      // we abort the transaction if we get to here, so changes get rolled back
      Session.currentSession.connection.rollback
      status
    }
  }

}

