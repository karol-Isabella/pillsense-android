package com.pillsense.app.feature.adherence.data

interface TestDataProvider {
    fun loadTestData()
    fun deleteTestData()
}

class NoOpTestDataProvider : TestDataProvider {
    override fun loadTestData() {}
    override fun deleteTestData() {}
}
