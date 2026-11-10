package hu.toliver.vinet.inference.litert

import org.bytedeco.javacpp.BytePointer
import org.bytedeco.javacpp.Loader
import org.bytedeco.tensorflowlite.BuiltinOpResolver
import org.bytedeco.tensorflowlite.FlatBufferModel
import org.bytedeco.tensorflowlite.Interpreter
import org.bytedeco.tensorflowlite.InterpreterBuilder
import org.bytedeco.tensorflowlite.global.tensorflowlite

/**
 * Host LiteRT interpreter for JVM tests. The Android build uses [BundledLiteRtSession]
 * (`com.google.ai.edge.litert:litert`). The Play-services runtime is not on this classpath.
 */
class JvmLiteRtSession(modelBytes: ByteArray) : LiteRtSession {
    private val modelBytesPointer: BytePointer
    private val model: FlatBufferModel
    private val interpreter: Interpreter

    init {
        Loader.load(tensorflowlite::class.java)
        modelBytesPointer = BytePointer(modelBytes.size.toLong())
        modelBytesPointer.put(modelBytes, 0, modelBytes.size)
        model = FlatBufferModel.BuildFromBuffer(modelBytesPointer, modelBytes.size.toLong())
        check(!model.isNull && model.initialized()) { "stand-in model failed to load" }
        val builder = InterpreterBuilder(model, BuiltinOpResolver())
        interpreter = Interpreter()
        check(builder.apply(interpreter) == 0) { "LiteRT interpreter rejected the stand-in" }
        check(interpreter.AllocateTensors() == 0) { "LiteRT AllocateTensors failed" }
    }

    override fun run(input: FloatArray): FloatArray {
        val inputTensor = interpreter.typed_input_tensor_float(0)
        for (index in input.indices) {
            inputTensor.put(index.toLong(), input[index])
        }
        check(interpreter.Invoke() == 0) { "LiteRT Invoke failed" }
        val count = (interpreter.output_tensor(0).bytes() / Float.SIZE_BYTES).toInt()
        val outputTensor = interpreter.typed_output_tensor_float(0)
        return FloatArray(count) { index -> outputTensor.get(index.toLong()) }
    }

    override fun close() {
        interpreter.close()
        model.close()
        modelBytesPointer.close()
    }
}
