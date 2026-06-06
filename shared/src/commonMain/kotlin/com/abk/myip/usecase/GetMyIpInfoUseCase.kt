package com.abk.myip.usecase

import com.abk.myip.data.IpInfoRepository
import com.abk.myip.domain.IpInfo

class GetMyIpInfoUseCase(private val repository: IpInfoRepository) {
    suspend operator fun invoke(): IpInfo = repository.getMyIpInfo()
}
