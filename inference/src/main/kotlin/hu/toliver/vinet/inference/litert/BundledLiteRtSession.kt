package hu.toliver.vinet.inference.litert

import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.tensorflow.lite.Interpreter

/**
 * Bundled LiteRT (`com.google.ai.edge.litert:litert`), not the Play services runtime (AD-9, AD-10).
 */
class BundledLiteRtSession(modelBytes: ByteArray) : LiteRtSession {
    private val interpreter: Interpreter
    private val inputBuffer: ByteBuffer
    private val outputBuffer: ByteBuffer

    init {
        val model = ByteBuffer.allocateDirect(modelBytes.size).order(ByteOrder.nativeOrder())
        model.put(modelBytes)
        model.rewind()
        interpreter = Interpreter(model)
        inputBuffer = ByteBuffer.allocateDirect(interpreter.getInputTensor(0).numBytes())
            .order(ByteOrder.nativeOrder())
        outputBuffer = ByteBuffer.allocateDirect(interpreter.getOutputTensor(0).numBytes())
            .order(ByteOrder.nativeOrder())
    }

    override fun run(input: FloatArray): FloatArray {
        inputBuffer.clear()
        for (value in input) {
            inputBuffer.putFloat(value)
        }
        inputBuffer.rewind()
        outputBuffer.clear()
        interpreter.run(inputBuffer, outputBuffer)
        outputBuffer.rewind()
        return FloatArray(outputBuffer.remaining() / Float.SIZE_BYTES) {
            outputBuffer.float
        }
    }

    override fun close() {
        interpreter.close()
    }
}
