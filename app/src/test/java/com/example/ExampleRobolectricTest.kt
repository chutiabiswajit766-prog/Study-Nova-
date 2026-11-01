package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("StudyNova AI", appName)
  }

  @Test
  fun `verify default exams loaded in repository`() {
    val exams = com.example.data.model.ExamRepository.supportedExams
    assertEquals(6, exams.size)
    assert(exams.any { it.id == "jee" })
    assert(exams.any { it.id == "neet" })
    assert(exams.any { it.id == "upsc" })
  }
}
