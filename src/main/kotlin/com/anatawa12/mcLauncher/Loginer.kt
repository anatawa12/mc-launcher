package com.anatawa12.mcLauncher

import com.mojang.authlib.Agent
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication
import java.net.Proxy

class Loginer(
    val userName: String,
    val password: String? = null,
    val proxy: Proxy = Proxy.NO_PROXY,
    clientToken: String = "1"
) {
    var clientToken: String = clientToken
        private set

    internal lateinit var auth: YggdrasilUserAuthentication
        private set

    fun login() {
        auth = YggdrasilAuthenticationService(
            proxy,
            clientToken
        ).createUserAuthentication(Agent.MINECRAFT) as YggdrasilUserAuthentication

        auth.setUsername(userName)
        if (password != null)
            auth.setPassword(password)
        auth.logIn()
    }
}
