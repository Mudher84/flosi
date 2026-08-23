package com.flosi.app.finance

import org.junit.Assert.assertEquals
import org.junit.Test

class BankFeedClassifierTest {
    private fun entry(amount:Long,description:String,id:String="x")=BankFeedEntry(
        externalId=id,amount=amount,description=description,occurredAt=1L,currency="IQD"
    )

    @Test fun arabicSalaryIsDetected(){
        assertEquals(BankFeedKind.SALARY,BankFeedClassifier.classify(entry(1_500_000,"راتب شهري وزارة التربية")))
    }

    @Test fun englishPayrollIsDetected(){
        assertEquals(BankFeedKind.SALARY,BankFeedClassifier.classify(entry(2_000_000,"PAYROLL AUGUST")))
    }

    @Test fun transferIsNotCountedAsIncomeOrExpense(){
        assertEquals(BankFeedKind.TRANSFER_IN,BankFeedClassifier.classify(entry(500_000,"Internal transfer")))
        assertEquals(BankFeedKind.TRANSFER_OUT,BankFeedClassifier.classify(entry(-500_000,"تحويل بين الحسابات")))
    }

    @Test fun ordinaryDebitIsExpense(){
        assertEquals(BankFeedKind.EXPENSE,BankFeedClassifier.classify(entry(-25_000,"POS MARKET")))
    }
}
