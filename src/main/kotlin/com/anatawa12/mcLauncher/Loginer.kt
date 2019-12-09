package com.anatawa12.mcLauncher

import com.mojang.authlib.Agent
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication
import java.net.Proxy

class Loginer {
    fun login(userName: String, password: String): YggdrasilUserAuthentication {
        val auth = YggdrasilAuthenticationService(
            Proxy.NO_PROXY,
            "1"
        ).createUserAuthentication(Agent.MINECRAFT) as YggdrasilUserAuthentication

        auth.setUsername(userName)
        auth.setPassword(password)
        auth.logIn()
        return auth
    }
}
