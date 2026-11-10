package hu.toliver.vinet.devtools

import hu.toliver.vinet.inference.litert.StandInStillScan
import hu.toliver.vinet.inference.parity.StandInParityCheck

/**
 * Debug-only hook for the stand-in parity check.
 * This source set is not compiled into the release build.
 */
object ParityHarnessHook {
    fun check(scan: StandInStillScan, samples: List<StandInParityCheck.Sample>): List<String> =
        StandInParityCheck.check(scan, samples)
}
