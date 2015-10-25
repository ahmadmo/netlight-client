package org.netlight.encoding;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.TooLongFrameException;

import java.util.List;

/**
 * @author ahmad
 */
public class JsonObjectDecoder extends ByteToMessageDecoder {

    private static final int ST_CORRUPTED = -1;
    private static final int ST_INIT = 0;
    private static final int ST_DECODING_NORMAL = 1;
    private static final int ST_DECODING_ARRAY_STREAM = 2;

    private final int maxObjectLength;
    private final boolean streamArrayElements;

    private int openBraces;
    private int idx;
    private int state;
    private boolean insideString;

    public JsonObjectDecoder() {
        this(1024 * 1024);
    }

    public JsonObjectDecoder(int maxObjectLength) {
        this(maxObjectLength, false);
    }

    public JsonObjectDecoder(boolean streamArrayElements) {
        this(1024 * 1024, streamArrayElements);
    }

    public JsonObjectDecoder(int maxObjectLength, boolean streamArrayElements) {
        if (maxObjectLength < 1) {
            throw new IllegalArgumentException("maxObjectLength must be a positive int");
        }
        this.maxObjectLength = maxObjectLength;
        this.streamArrayElements = streamArrayElements;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (state == ST_CORRUPTED) {
            in.skipBytes(in.readableBytes());
            return;
        }
        int idx = this.idx;
        int wrtIdx = in.writerIndex();
        if (wrtIdx > maxObjectLength) {
            in.skipBytes(in.readableBytes());
            reset();
            throw new TooLongFrameException("object length exceeds " + maxObjectLength + ": " + wrtIdx + " bytes discarded");
        }
        for (; idx < wrtIdx; idx++) {
            byte c = in.getByte(idx);
            if (state == ST_DECODING_NORMAL) {
                decodeByte(c, in, idx);
                if (openBraces == 0) {
                    ByteBuf json = extractObject(ctx, in, in.readerIndex(), idx + 1 - in.readerIndex());
                    if (json != null) {
                        out.add(json);
                    }
                    in.readerIndex(idx + 1);
                    reset();
                }
            } else if (state == ST_DECODING_ARRAY_STREAM) {
                decodeByte(c, in, idx);
                if (!insideString && (openBraces == 1 && c == ',' || openBraces == 0 && c == ']')) {
                    for (int i = in.readerIndex(); Character.isWhitespace(in.getByte(i)); i++) {
                        in.skipBytes(1);
                    }
                    int idxNoSpaces = idx - 1;
                    while (idxNoSpaces >= in.readerIndex() && Character.isWhitespace(in.getByte(idxNoSpaces))) {
                        idxNoSpaces--;
                    }
                    ByteBuf json = extractObject(ctx, in, in.readerIndex(), idxNoSpaces + 1 - in.readerIndex());
                    if (json != null) {
                        out.add(json);
                    }
                    in.readerIndex(idx + 1);
                    if (c == ']') {
                        reset();
                    }
                }
            } else if (c == '{' || c == '[') {
                initDecoding(c);
                if (state == ST_DECODING_ARRAY_STREAM) {
                    in.skipBytes(1);
                }
            } else if (Character.isWhitespace(c)) {
                in.skipBytes(1);
            } else {
                state = ST_CORRUPTED;
                throw new CorruptedFrameException("invalid JSON received at byte position " + idx + ": " + ByteBufUtil.hexDump(in));
            }
        }
        if (in.readableBytes() == 0) {
            this.idx = 0;
        } else {
            this.idx = idx;
        }
    }

    @SuppressWarnings("UnusedParameters")
    protected ByteBuf extractObject(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length) {
        return buffer.slice(index, length).retain();
    }

    private void decodeByte(byte c, ByteBuf in, int idx) {
        if ((c == '{' || c == '[') && !insideString) {
            openBraces++;
        } else if ((c == '}' || c == ']') && !insideString) {
            openBraces--;
        } else if (c == '"') {
            if (!insideString) {
                insideString = true;
            } else if (in.getByte(idx - 1) != '\\') {
                insideString = false;
            }
        }
    }

    private void initDecoding(byte openingBrace) {
        openBraces = 1;
        if (openingBrace == '[' && streamArrayElements) {
            state = ST_DECODING_ARRAY_STREAM;
        } else {
            state = ST_DECODING_NORMAL;
        }
    }

    private void reset() {
        insideString = false;
        state = ST_INIT;
        openBraces = 0;
    }

}