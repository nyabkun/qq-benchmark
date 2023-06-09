/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

@file:Suppress("NOTHING_TO_INLINE", "FunctionName")

package nyab.test

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import kotlin.reflect.KFunction
import nyab.conf.QE
import nyab.conf.QMyMark
import nyab.match.QMMethod
import nyab.match.and
import nyab.match.or
import nyab.util.QException
import nyab.util.QLogStyle
import nyab.util.QOnlyIfStr
import nyab.util.QOut
import nyab.util.QShColor
import nyab.util.QSrcCut
import nyab.util.blue
import nyab.util.green
import nyab.util.light_blue
import nyab.util.light_red
import nyab.util.qBrackets
import nyab.util.qCallerFileName
import nyab.util.qColor
import nyab.util.qFormatDuration
import nyab.util.qIsInstanceMethod
import nyab.util.qLogStackFrames
import nyab.util.qMethods
import nyab.util.qName
import nyab.util.qNewInstance
import nyab.util.qSeparatorWithLabel
import nyab.util.qStackFrameEntryMethod
import nyab.util.qThisFileMainClass
import nyab.util.qTimeIt
import nyab.util.qTrySetAccessible
import nyab.util.re
import nyab.util.red
import nyab.util.separator
import nyab.util.throwIt
import nyab.util.yellow

// qq-benchmark is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=4] = QTest <-[Ref]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
internal annotation class QTest(val testOnlyThis: Boolean = false)

// CallChain[size=2] = QTestHumanCheckRequired <-[Call]- QBenchmarkTest.cachedRegex()[Root]
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
internal annotation class QTestHumanCheckRequired(val testOnlyThis: Boolean = false)

// CallChain[size=4] = QBeforeEach <-[Ref]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
internal annotation class QBeforeEach

// CallChain[size=4] = QAfterEach <-[Ref]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
internal annotation class QAfterEach

// CallChain[size=6] = QTestResultElement <-[Ref]- QTestResult.QTestResult() <-[Call]- QTestResult.numFail <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
internal data class QTestResultElement(val method: Method, val cause: Throwable?) {
    // CallChain[size=5] = QTestResultElement.success <-[Call]- QTestResult.numFail <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    val success: Boolean
        get() = cause == null
}

// CallChain[size=6] = List<QTestResultElement>.allTestedMethods <-[Call]- QTestResult.printIt() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
internal val List<QTestResultElement>.allTestedMethods: String
    get() =
        "\n[${"Tested".light_blue}]\n" +
                this.joinToString("\n") {
                    if (it.success) {
                        it.method.qName().green
                    } else {
                        it.method.qName().light_red
                    }
                }

// CallChain[size=3] = QTestResult <-[Ref]- qTestHumanCheck() <-[Call]- main()[Root]
internal class QTestResult(val elements: List<QTestResultElement>, val time: Long) {
    // CallChain[size=6] = QTestResult.targetClasses <-[Call]- QTestResult.printIt() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    val targetClasses = elements.map { it.method.declaringClass.canonicalName }

    // CallChain[size=7] = QTestResult.numSuccess <-[Call]- QTestResult.str <-[Call]- QTestResult.printIt() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    val numSuccess = elements.count { it.success }
    // CallChain[size=4] = QTestResult.numFail <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    val numFail = elements.count { !it.success }

    // CallChain[size=6] = QTestResult.str <-[Call]- QTestResult.printIt() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    val str = qBrackets("Result".blue, "$numSuccess / ${numFail + numSuccess}", "Time".blue, time.qFormatDuration())

    // CallChain[size=5] = QTestResult.printIt() <-[Call]- qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
    fun printIt(out: QOut = QOut.CONSOLE) {
        out.separator(start = "")

        if (numFail > 0) {
            out.println("Fail ...".red)
            out.println(str)
            out.println(elements.allTestedMethods)

            elements.filter { !it.success }.forEach { ele ->
                out.separator()

                val cause = if (ele.cause != null && ele.cause is QException) {
                    ele.cause
                } else if (ele.cause?.cause != null && ele.cause.cause is QException) {
                    ele.cause.cause
                } else {
                    ele.cause
                }

                val causeStr = if (cause != null && cause is QException) {
                    cause.type.name
                } else if (cause != null) {
                    cause::class.simpleName ?: cause::class.java.simpleName
                } else {
                    "null"
                }

                val msg = qBrackets("Failed".light_blue, ele.method.name.red, "Cause".light_blue, causeStr.red)

                val mySrcAndMsg = if (cause != null && cause is QException) {
                    val stackColoringRegex = targetClasses.joinToString("|") { ta ->
                        """(.*($ta|${ta}Kt).*?)\("""
                    }.re

                    val stackStr = stackColoringRegex.replace(cause.mySrcAndStack, "$1".qColor(QShColor.Blue) + "(")

                    cause.message + "\n\n" + stackStr
                } else {
                    ""
                }

                if (mySrcAndMsg.isNotEmpty()) {
                    out.println(msg + "\n")
                    out.println(mySrcAndMsg)
                } else if (cause != null) {
                    out.println(msg)
                    out.println("StackTrace >>>>>")
                    out.println(cause.stackTraceToString())
                }
            }
        } else {
            out.println("${"✨".yellow} ${" Success ".green} ${"✨".yellow}".green + "\n")
            out.println(str)
            out.println(elements.allTestedMethods)
        }
    }
}

// CallChain[size=4] = qTestMethods() <-[Call]- qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
private fun qTestMethods(
    methodsToTest: List<Method>,
    beforeMethod: List<Method> = emptyList(),
    afterMethod: List<Method> = emptyList(),
    out: QOut = QOut.CONSOLE
): QTestResult {
    val results = mutableListOf<QTestResultElement>()

    val timeItResult = qTimeIt(quiet = true) {
        for (method in methodsToTest) {
            // "⭐"
            out.println(qSeparatorWithLabel("${QMyMark.test_method} " + method.qName(true)))

            method.qTrySetAccessible()

            val cause =
                if (method.qIsInstanceMethod()) {
                    val testInstance = method.declaringClass.qNewInstance()

                    try {
                        for (before in beforeMethod) {
                            before.invoke(testInstance)
                        }

                        method.invoke(testInstance)

                        for (after in afterMethod) {
                            after.invoke(testInstance)
                        }
                        null
                    } catch (e: Throwable) {
                        e
                    }
                } else {
                    try {
                        for (before in beforeMethod) {
                            before.invoke(null)
                        }

                        method.invoke(null)

                        for (after in afterMethod) {
                            after.invoke(null)
                        }
                        null
                    } catch (e: Throwable) {
                        e
                    }
                }

            results += if (cause?.cause != null && cause is InvocationTargetException) {
                QTestResultElement(method, cause.cause)
            } else {
                QTestResultElement(method, cause)
            }
        }
    }

    val result = QTestResult(results, timeItResult.time)
    result.printIt()

    return result
}

// CallChain[size=3] = qTest() <-[Call]- qTestHumanCheck() <-[Call]- main()[Root]
internal fun qTest(
    vararg targetClasses: Class<*> = arrayOf(qThisFileMainClass),

    targetMethodFilter: QMMethod =
        (QMMethod.annotation(QTest::class) or QMMethod.annotation("Test")) and
                QMMethod.notAnnotation(QTestHumanCheckRequired::class) and
//                QMMethod.notAnnotation(QIgnore::class) and
                QMMethod.DeclaredOnly and
                QMMethod.NoParams and
                QMMethod.nameNotExact("main"),

    beforeMethodFilter: QMMethod =
        (
                QMMethod.annotation(QBeforeEach::class) or QMMethod.annotation("BeforeTest")
                        or QMMethod.annotation("BeforeEach")
                        or QMMethod.annotation("BeforeMethod")
                )
                and QMMethod.DeclaredOnly and QMMethod.NoParams and QMMethod.nameNotExact(
            "main"
        ),

    afterMethodFilter: QMMethod =
        (
                QMMethod.annotation(QAfterEach::class) or QMMethod.annotation("AfterTest")
                        or QMMethod.annotation("AfterEach")
                        or QMMethod.annotation("AfterMethod")
                ) and QMMethod.DeclaredOnly and QMMethod.NoParams and QMMethod.nameNotExact(
            "main"
        ),

    out: QOut = QOut.CONSOLE,

    throwsException: Boolean = true,
): QTestResult {
    val targets = targetClasses.joinToString(", ") { it.simpleName }

    out.separator()

    val callerFileName = qCallerFileName()

    val methodsToTestImmediately = targetClasses.flatMap { cls ->
        cls.qMethods().filter { method ->
            (QMMethod.DeclaredOnly and (
                    QMMethod.annotation(QTest::class) { it.testOnlyThis } or
                            QMMethod.annotation(QTestHumanCheckRequired::class) { it.testOnlyThis })).matches(method)
        }.sortedBy {
            it.name // TODO sort by line number
        }
    }

    val methodsToTest = methodsToTestImmediately.ifEmpty {
        targetClasses.flatMap {
            it.qMethods().filter { method ->
                targetMethodFilter.matches(method)
            }
        }.sortedBy {
            it.name // TODO sort by line number
        }
    }

    qLogStackFrames(
        // "🚀"
        msg = "${QMyMark.test_start} Test Start ${QMyMark.test_start}\n$targets".light_blue,
        style = QLogStyle.MSG_AND_STACK,
        frames = listOf(
            qStackFrameEntryMethod { frame ->
                frame.fileName == callerFileName
            }
        )
    )

    val before = targetClasses.flatMap {
        it.qMethods().filter { method ->
            beforeMethodFilter.matches(method)
        }
    }

    val after = targetClasses.flatMap {
        it.qMethods().filter { method ->
            afterMethodFilter.matches(method)
        }
    }

    val result = qTestMethods(methodsToTest, before, after)

    if (result.numFail > 0 && throwsException) {
        QE.TestFail.throwIt()
    } else {
        return result
    }
}

// CallChain[size=2] = qTestHumanCheck() <-[Call]- main()[Root]
internal fun qTestHumanCheck(vararg targetClasses: Class<*> = arrayOf(qThisFileMainClass)): QTestResult {
    return qTest(
        targetClasses = targetClasses,
        targetMethodFilter =
        QMMethod.annotation(QTestHumanCheckRequired::class) and
//                QMMethod.notAnnotation(QIgnore::class) and
                QMMethod.DeclaredOnly and
                QMMethod.NoParams and
                QMMethod.nameNotExact("main")
    )
}