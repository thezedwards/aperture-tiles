/*
 * Copyright (c) 2014 Oculus Info Inc.
 * http://www.oculusinfo.com/
 *
 * Released under the MIT License.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oculusinfo.tilegen.datasets

import java.io.{FileWriter, File}
import java.util.Properties

import com.oculusinfo.binning.TileIndex
import com.oculusinfo.tilegen.binning.{OnDemandBinningPyramidIO, OnDemandAccumulatorPyramidIO}
import com.oculusinfo.tilegen.tiling.TestTileIO
import com.oculusinfo.tilegen.util.PropertiesWrapper
import org.apache.spark.{SparkContext, SharedSparkContext}
import org.scalatest.{BeforeAndAfterAll, FunSuite}

/**
 * Created by nkronenfeld on 8/21/2015.
 */
class TilingTaskLineBinningTestSuite  extends FunSuite with SharedSparkContext with BeforeAndAfterAll with TileAssertions {
	val pyramidId = "segmentTest"
	val tileIO = new TestTileIO
	var props: Properties = null
	var dataFile: File = null

	override def beforeAll = {
		super.beforeAll
		createDataset(sc)
	}

	override def afterAll = {
		cleanupDataset
		super.afterAll
	}

	private def createDataset(sc: SparkContext): Unit = {
		// Create our data
		// We create a simple data set with pairs (n, 7-n) as n goes from 0 to 6
		dataFile = File.createTempFile("simple-live-segment-tile-test", ".csv")
		println("Creating temporary data file " + dataFile.getAbsolutePath())
		val writer = new FileWriter(dataFile)
		// Line N goes from (0, n) to (n, n) = horizontal line N long.
		Range(0, 15).foreach(n =>
			writer.write("%f,%f,%f,%f\n".format(0.5, n+0.5, n+0.5, n+0.5))
		)
		writer.flush()
		writer.close()
		val rawData = sc.textFile(dataFile.getAbsolutePath)

		// Read the one into the other
		props = new Properties()
		props.setProperty("oculus.binning.source.location.0", dataFile.getAbsolutePath())
		props.setProperty("oculus.binning.projection.autobounds", "false")
		props.setProperty("oculus.binning.projection.type", "areaofinterest")
		props.setProperty("oculus.binning.projection.minX", "0.0")
		props.setProperty("oculus.binning.projection.maxX", "15.9999")
		props.setProperty("oculus.binning.projection.minY", "0.0")
		props.setProperty("oculus.binning.projection.maxY", "15.9999")
		props.setProperty("oculus.binning.parsing.separator", ",")
		props.setProperty("oculus.binning.parsing.x1.index", "0")
		props.setProperty("oculus.binning.parsing.y1.index", "1")
		props.setProperty("oculus.binning.parsing.x2.index", "2")
		props.setProperty("oculus.binning.parsing.y2.index", "3")
		props.setProperty("oculus.binning.index.type", "cartesian")
		props.setProperty("oculus.binning.index.field.0", "x1")
		props.setProperty("oculus.binning.index.field.1", "y1")
		props.setProperty("oculus.binning.index.field.2", "x2")
		props.setProperty("oculus.binning.index.field.3", "y2")
		props.setProperty("oculus.binning.levels.0", "2")
		props.setProperty("oculus.binning.name", pyramidId)
		props.setProperty("oculus.binning.minimumSegmentLength", "4")
		props.setProperty("oculus.binning.maximumSegmentLength", "8")
		props.setProperty("oculus.binning.lineType", "Lines")
		props.setProperty("oculus.binning.tileWidth", "4")
		props.setProperty("oculus.binning.tileHeight", "4")

		val parseConfiguration =  new PropertiesWrapper(props)
		val reader = new CSVReader(sqlc, rawData, parseConfiguration)
		val data = reader.asDataFrame
		data.registerTempTable(pyramidId)
	}

	private def cleanupDataset: Unit = {
		if (dataFile.exists) {
			sqlc.dropTempTable(pyramidId)
			println("Deleting temporary data file " + dataFile)
			dataFile.delete
		}
		dataFile = null
	}

	test("Test simple line tiling") {
		val task = TilingTask(sqlc, pyramidId, props)

		task.doLineTiling(tileIO)



		val tile200 = tileIO.getTile(pyramidId, new TileIndex(2, 0, 0, 4, 4))
		val tile201 = tileIO.getTile(pyramidId, new TileIndex(2, 0, 1, 4, 4))
		val tile202 = tileIO.getTile(pyramidId, new TileIndex(2, 0, 2, 4, 4))
		val tile203 = tileIO.getTile(pyramidId, new TileIndex(2, 0, 3, 4, 4))

		val tile210 = tileIO.getTile(pyramidId, new TileIndex(2, 1, 0, 4, 4))
		val tile211 = tileIO.getTile(pyramidId, new TileIndex(2, 1, 1, 4, 4))
		val tile212 = tileIO.getTile(pyramidId, new TileIndex(2, 1, 2, 4, 4))
		val tile213 = tileIO.getTile(pyramidId, new TileIndex(2, 1, 3, 4, 4))

		val tile220 = tileIO.getTile(pyramidId, new TileIndex(2, 2, 0, 4, 4))
		val tile221 = tileIO.getTile(pyramidId, new TileIndex(2, 2, 1, 4, 4))
		val tile222 = tileIO.getTile(pyramidId, new TileIndex(2, 2, 2, 4, 4))
		val tile223 = tileIO.getTile(pyramidId, new TileIndex(2, 2, 3, 4, 4))

		val tile230 = tileIO.getTile(pyramidId, new TileIndex(2, 3, 0, 4, 4))
		val tile231 = tileIO.getTile(pyramidId, new TileIndex(2, 3, 1, 4, 4))
		val tile232 = tileIO.getTile(pyramidId, new TileIndex(2, 3, 2, 4, 4))
		val tile233 = tileIO.getTile(pyramidId, new TileIndex(2, 3, 3, 4, 4))



		assert(tile200.isEmpty)
		assert(tile210.isEmpty)
		assert(tile220.isEmpty)
		assert(tile230.isEmpty)

		assertTileContents(List(
			                   1.0, 1.0, 1.0, 1.0,
			                   1.0, 1.0, 1.0, 1.0,
			                   1.0, 1.0, 1.0, 1.0,
			                   1.0, 1.0, 1.0, 1.0
		                   ), tile201.get)
		assertTileContents(List(
			                   1.0, 1.0, 1.0, 1.0,
			                   1.0, 1.0, 1.0, 0.0,
			                   1.0, 1.0, 0.0, 0.0,
			                   1.0, 0.0, 0.0, 0.0
		                   ), tile211.get)
		assert(tile221.isEmpty)
		assert(tile231.isEmpty)

		assert(tile202.isEmpty)
		assert(tile212.isEmpty)
		assert(tile222.isEmpty)
		assert(tile232.isEmpty)

		assert(tile203.isEmpty)
		assert(tile213.isEmpty)
		assert(tile223.isEmpty)
		assert(tile233.isEmpty)
	}
}
