package com.rubyboat.pseudolib.mixin;

import com.rubyboat.pseudolib.Pseudolib;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(ServerPropertiesHandler.class)
public class ServerPropertiesHandlerMixin {


    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/ServerPropertiesHandler;getServerResourcePackProperties(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;)Ljava/util/Optional;"))
    public Optional<MinecraftServer.ServerResourcePackProperties> getResourcePackProperties(String url, String sha1, @Nullable String hash, boolean required, String prompt) {
        return Optional.of(new MinecraftServer.ServerResourcePackProperties("http://localhost:8000/" + Pseudolib.RESOURCE_PACK_PATH, "", true, Text.literal("Download or else")));
    }
}
