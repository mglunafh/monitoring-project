package org.burufi.monitoring.delivery

import org.burufi.monitoring.delivery.model.Transport
import org.burufi.monitoring.delivery.model.TransportCategory
import org.burufi.monitoring.delivery.model.TransportStatus
import org.burufi.monitoring.delivery.model.TransportType
import org.burufi.monitoring.delivery.repository.TransportRepository
import org.burufi.monitoring.delivery.repository.TransportTypeRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@Profile("default")
class TransportPopulator(
    val transportTypeRepository: TransportTypeRepository,
    val parkRepository: TransportRepository
) : CommandLineRunner {

    companion object {
        const val VOLVO_FH13 = "Volvo FH13 2012"
        const val GAZELLE = "GAZel 2005"
        const val KIA_RIO = "Kia Rio 2017"
        const val MI_2 = "Mi-2"
        const val MI_8T = "Mi-8T"
    }

    override fun run(vararg args: String?) {
        transportTypes()
        TransportCategory.entries.forEach {
            println("$it: ${transportTypeRepository.findByCategory(it)}")
        }

        transportPark()
        parkRepository.findAll().forEach {
            println("Transport(id=${it.id}, category=${it.transportType.category}, mark=${it.transportType.mark}, status=${it.status})")
        }

        println("=========== AVAILABLE")

        parkRepository.findByStatusAndTransportTypeMark(TransportStatus.AVAILABLE, GAZELLE).forEach {
            println("Transport(id=${it.id}, category=${it.transportType.category}, mark=${it.transportType.mark}, status=${it.status})")
        }

        println("=========== FIRST AVAILABLE")

        parkRepository.findFirstByTransportTypeMarkAndStatus(GAZELLE)?.let {
            println("Transport(id=${it.id}, category=${it.transportType.category}, mark=${it.transportType.mark}, status=${it.status})")
        }

    }

    private fun transportTypes() {

        val fh13 = TransportType(
            category = TransportCategory.TRUCK,
            mark = VOLVO_FH13,
            maxCargo = 15_000,
            maxDistance = 2000,
            speed = 60,
            pricePerDistance = BigDecimal(100)
        )

        val gazelle = TransportType(
            category = TransportCategory.TRUCK,
            mark = GAZELLE,
            maxCargo = 5_000,
            maxDistance = 2000,
            speed = 80,
            pricePerDistance = BigDecimal(70)
        )

        val kiaRio = TransportType(
            category = TransportCategory.LIGHT_CAR,
            mark = KIA_RIO,
            maxCargo = 100,
            maxDistance = 500,
            speed = 80,
            pricePerDistance = BigDecimal(50)
        )

        val mi2 = TransportType(
            category = TransportCategory.HELICOPTER,
            mark = MI_2,
            maxCargo = 800,
            maxDistance = 1800,
            speed = 190,
            pricePerDistance = BigDecimal(240)      // 240 kg per hour
        )

        val mi8t = TransportType(
            category = TransportCategory.HELICOPTER,
            mark = MI_8T,
            maxCargo = 3_000,
            maxDistance = 2850,
            speed = 230,
            pricePerDistance = BigDecimal(620)      // 620 kg per hour
        )

        val types = listOf(fh13, gazelle, kiaRio, mi2, mi8t)
        transportTypeRepository.saveAll(types)
    }

    private fun transportPark() {
        val rioCount = 5
        val kiaType = transportTypeRepository.findByMark(KIA_RIO)
        kiaType?.also { kia ->
            val someKias = List(rioCount) { Transport(transportType = kia) }
            parkRepository.saveAll(someKias)
        }

        val gazelCount = 3
        val gazelType = transportTypeRepository.findByMark(GAZELLE)
        gazelType?.also { gazel ->
            val someGazels = List(gazelCount) { Transport(transportType = gazel)}
            parkRepository.saveAll(someGazels)
        }

        transportTypeRepository.findByMark(MI_2)?.also { parkRepository.save(Transport(transportType = it)) }
        transportTypeRepository.findByMark(MI_8T)?.also { parkRepository.save(Transport(transportType = it)) }
    }
}