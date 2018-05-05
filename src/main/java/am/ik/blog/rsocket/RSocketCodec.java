package am.ik.blog.rsocket;

import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.Encoder;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.codec.json.Jackson2SmileDecoder;
import org.springframework.http.codec.json.Jackson2SmileEncoder;

public enum RSocketCodec {
	JSON(new Jackson2JsonEncoder(), new Jackson2JsonDecoder(), MediaType.APPLICATION_JSON,
			new MediaType("application", "stream+json")), SMILE(
					new Jackson2SmileEncoder(), new Jackson2SmileDecoder(),
					new MediaType("application", "x-jackson-smile"),
					new MediaType("application", "stream+x-jackson-smile"));

	private final Encoder<Object> encoder;
	private final Decoder<Object> decoder;
	private final MediaType singleMediaType;
	private final MediaType streamMediaType;

	RSocketCodec(Encoder<Object> encoder, Decoder<Object> decoder,
				 MediaType singleMediaType, MediaType streamMediaType) {
		this.encoder = encoder;
		this.decoder = decoder;
		this.singleMediaType = singleMediaType;
		this.streamMediaType = streamMediaType;
	}

	public Encoder<Object> encoder() {
		return encoder;
	}

	public Decoder<Object> decoder() {
		return decoder;
	}

	public MediaType singleMediaType() {
		return singleMediaType;
	}

	public MediaType streamMediaType() {
		return streamMediaType;
	}
}
