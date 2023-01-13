package studio.robotmonkey.sha1runtime.mixin;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ResourcePackSendS2CPacket;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import studio.robotmonkey.sha1runtime.SHA1Runtime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

@Mixin(ResourcePackSendS2CPacket.class)
public class ResourcePackMixin {

    @Mutable
    @Shadow @Final
    private final String url;

    @Mutable
    @Shadow @Final
    private final String hash;
    @Mutable
    @Shadow @Final
    private final boolean required;

    @Mutable
    @Shadow @Final @Nullable
    private final Text prompt;

    public ResourcePackMixin(String url, String hash, boolean required, @Nullable Text prompt) {
        if (hash.length() > 40) {
            throw new IllegalArgumentException("Hash is too long (max 40, was " + hash.length() + ")");
        } else {
            this.url = url;
            this.hash = hash;
            this.required = required;
            this.prompt = prompt;
        }
    }


    @Inject(at = @At("HEAD"), method = "write(Lnet/minecraft/network/PacketByteBuf;)V", cancellable = true)
    public void write(PacketByteBuf buf, CallbackInfo ci) {
        buf.writeString(this.url);
        if(this.prompt != null){
            SHA1Runtime.LOGGER.info(this.prompt.getString());
        }
        File hashFile = new File("config/ResourcePackHash.txt");
        try {
            Scanner fileReader = new Scanner(hashFile);
            if(fileReader.hasNextLine()) {
                String hash = fileReader.nextLine();
                SHA1Runtime.LOGGER.info("Hash Found: " + hash);
                buf.writeString(hash);
            } else {
                SHA1Runtime.LOGGER.warn("No Hash in file: Please open config folder and add your hash.");
                buf.writeString(this.hash);
            }
            fileReader.close();
        } catch(FileNotFoundException e)
        {
            SHA1Runtime.LOGGER.error("Missing Hash File! Generating Now...");
            try {
                hashFile.createNewFile();
            } catch(IOException ioException)
            {
                ioException.printStackTrace();
            }
            buf.writeString(this.hash);
        }

        buf.writeBoolean(this.required);
        buf.writeNullable(this.prompt, PacketByteBuf::writeText);
        ci.cancel();
    }
}
