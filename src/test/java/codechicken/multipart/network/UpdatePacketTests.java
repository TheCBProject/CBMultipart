package codechicken.multipart.network;

import codechicken.lib.data.MCDataByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Mainly used for manual validation.
 * Created by covers1624 on 4/30/20.
 */
public class UpdatePacketTests {

    @BeforeEach
    public void setup() {
        MultiPartCPH.lastPos = new BlockPos(0, 0, 0);
    }

    @Test
    public void testFirstPacket() {
        ByteBuf buf = Unpooled.buffer();
        MCDataByteBuf packet = new MCDataByteBuf(buf);
        BlockPos last = new BlockPos(0, 0, 0);
        BlockPos curr = new BlockPos(3333, 56, -2348);
        MultiPartSPH.writeHeader(packet, 44, last, curr);

        BlockPos readPos = MultiPartCPH.readUpdateHeader(buf.readByte() & 0x03, packet);
        assertEquals(curr, readPos);
    }

    @Test
    public void testSubChunk() {
        ByteBuf buf = Unpooled.buffer();
        MCDataByteBuf packet = new MCDataByteBuf(buf);
        BlockPos last = new BlockPos(0, 0, 0);
        BlockPos curr = new BlockPos(3333, 56, -2348);
        MultiPartSPH.writeHeader(packet, 44, last, curr);

        BlockPos readPos = MultiPartCPH.readUpdateHeader(packet.readByte() & 0x03, packet);
        assertEquals(curr, readPos);
        buf.readerIndex(0);
        buf.writerIndex(0);
        last = curr;
        // 3333 & 15 = 5
        // -2348 & 15 = 4
        curr = curr.offset(3, 6, 9);
        MultiPartSPH.writeHeader(packet, 44, last, curr);
        assertEquals(3, buf.writerIndex());//Should be exactly 3 bytes written, +1 because this is the next index.

        readPos = MultiPartCPH.readUpdateHeader(packet.readByte() & 0x03, packet);

        assertEquals(curr, readPos);
    }

    @Test
    public void testSameBlock() {
        ByteBuf buf = Unpooled.buffer();
        MCDataByteBuf packet = new MCDataByteBuf(buf);
        BlockPos last = new BlockPos(0, 0, 0);
        BlockPos curr = new BlockPos(3333, 56, -2348);
        MultiPartSPH.writeHeader(packet, 44, last, curr);

        BlockPos readPos = MultiPartCPH.readUpdateHeader(packet.readByte() & 0x03, packet);
        assertEquals(curr, readPos);
        buf.readerIndex(0);
        buf.writerIndex(0);

        last = curr;
        MultiPartSPH.writeHeader(packet, 44, last, curr);
        assertEquals(1, buf.writerIndex());//Should be exactly 1 byte written, +1 because this is the next index.

        readPos = MultiPartCPH.readUpdateHeader(packet.readByte() & 0x03, packet);
        assertEquals(curr, readPos);
    }

    @Test
    public void testWithin128() {
        ByteBuf buf = Unpooled.buffer();
        MCDataByteBuf packet = new MCDataByteBuf(buf);
        BlockPos last = new BlockPos(0, 0, 0);
        BlockPos curr = new BlockPos(3333, 56, -2348);
        MultiPartSPH.writeHeader(packet, 44, last, curr);

        BlockPos readPos = MultiPartCPH.readUpdateHeader(packet.readByte() & 0x03, packet);
        assertEquals(curr, readPos);
        buf.readerIndex(0);
        buf.writerIndex(0);

        last = curr;
        curr = curr.offset(127, 6, 9);
        MultiPartSPH.writeHeader(packet, 44, last, curr);
        assertEquals(4, buf.writerIndex());//Should be exactly 4 bytes written, +1 because this is the next index.

        readPos = MultiPartCPH.readUpdateHeader(packet.readByte() & 0x03, packet);
        assertEquals(curr, readPos);
    }
}
