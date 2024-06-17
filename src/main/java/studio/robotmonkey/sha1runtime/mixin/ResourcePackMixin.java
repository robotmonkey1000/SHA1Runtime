package studio.robotmonkey.sha1runtime.mixin;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import studio.robotmonkey.sha1runtime.SHA1Runtime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

@Mixin(ResourcePackSendS2CPacket.class)
public class ResourcePackMixin {

    @Inject(at= @At("HEAD"), method = "hash()Ljava/lang/String;", cancellable = true)
    public void getHash(CallbackInfoReturnable ci)
    {
        File hashFile = new File("config/ResourcePackHash.txt");
        try {
            Scanner fileReader = new Scanner(hashFile);
            if (fileReader.hasNextLine()) {
                String hashInFile = fileReader.nextLine();
                SHA1Runtime.LOGGER.info("Hash Found: " + hashInFile);
                ci.setReturnValue(hashInFile);
            } else {
                SHA1Runtime.LOGGER.warn("No Hash in file: Please open config folder and add your hash.");
            }
            fileReader.close();
        } catch (FileNotFoundException e) {
            SHA1Runtime.LOGGER.error("Missing Hash File! Generating Now...");
            try {
                hashFile.createNewFile();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }




//    @Inject(at = @At("HEAD"), method = "write(Lnet/minecraft/network/PacketByteBuf;)V", cancellable = true)
//    public void write(PacketByteBuf buf, CallbackInfo ci) {
//        buf.writeUuid(id);
//        buf.writeString(url);
//        if (prompt != null) {
//            SHA1Runtime.LOGGER.info(prompt.getString());
//        }
//        File hashFile = new File("config/ResourcePackHash.txt");
//        try {
//            Scanner fileReader = new Scanner(hashFile);
//            if (fileReader.hasNextLine()) {
//                String hash = fileReader.nextLine();
//                SHA1Runtime.LOGGER.info("Hash Found: " + hash);
//                buf.writeString(hash);
//            } else {
//                SHA1Runtime.LOGGER.warn("No Hash in file: Please open config folder and add your hash.");
//                buf.writeString(hash);
//            }
//            fileReader.close();
//        } catch (FileNotFoundException e) {
//            SHA1Runtime.LOGGER.error("Missing Hash File! Generating Now...");
//            try {
//                hashFile.createNewFile();
//            } catch (IOException ioException) {
//                ioException.printStackTrace();
//            }
//            buf.writeString(hash);
//        }
//
//        buf.writeBoolean(required);
//        buf.writeNullable(prompt, PacketByteBuf::writeText);
//        ci.cancel();
//    }


}
