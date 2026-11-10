package hu.toliver.vinet.inference.litert

/** One loaded LiteRT model. Input and output are flat NHWC float tensors. */
interface LiteRtSession : AutoCloseable {
    fun run(input: FloatArray): FloatArray
}
