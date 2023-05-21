/*
 * Copyright 2023. nyabkun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nyab.conf

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import nyab.match.QM
import nyab.util.QFType
import nyab.util.path
import nyab.util.qListByMatch

// qq-benchmark is a self-contained single-file library created by nyabkun.
// This is a split-file version of the library, this file is not self-contained.

// CallChain[size=17] = QMyPath <-[Ref]- qLogStackFrames() <-[Call]- QException.mySrcAndStack <-[Cal ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
internal object QMyPath {
    // -- dirs

    // CallChain[size=18] = QMyPath.src <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <-[Call]- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val src = "src".path
    // CallChain[size=18] = QMyPath.src_java <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <-[C ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val src_java = "src-java".path
    // CallChain[size=18] = QMyPath.src_build <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <-[ ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val src_build = "src-build".path
    // CallChain[size=18] = QMyPath.src_experiment <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames( ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val src_experiment = "src-experiment".path
    // CallChain[size=18] = QMyPath.src_plugin <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val src_plugin = "src-plugin".path
    // CallChain[size=18] = QMyPath.src_config <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <- ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val src_config = "src-config".path
    // CallChain[size=18] = QMyPath.src_test <-[Call]- QMyPath.src_root <-[Call]- qLogStackFrames() <-[C ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val src_test = "src-test".path
    // --- dir list
    // CallChain[size=17] = QMyPath.src_root <-[Call]- qLogStackFrames() <-[Call]- QException.mySrcAndSt ... n <-[Propag]- QBlockLoop <-[Call]- QBenchmark.block() <-[Call]- QBenchmarkTest.cachedRegex()[Root]
    val src_root: List<Path> by lazy {
        val base = listOf(
            src,
            src_test,
            src_experiment,
            src_config,
            src_plugin,
            src_java,
            src_build,
            "src".path,
            "test".path,
            "src/main/kotlin".path,
            "src/test/kotlin".path,
            "src/main/java".path,
            "src/test/java".path,
//            ".".path,
        ).filter { it.exists() }

        val search = Paths.get(".").qListByMatch(type = QFType.Dir, nameMatch = QM.startsWith("src-"), maxDepth = 1)

        (base + search).distinct()
    }

    // -- files
}