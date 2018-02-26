package org.adorsys.encobject.service.impl;

import com.nimbusds.jose.CompressionAlgorithm;
import com.nimbusds.jose.JWEHeader;
import org.adorsys.encobject.domain.ContentMetaInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ContentMetaInfoUtils {
	
	public static final void header2MetaInfo(final JWEHeader header, final ContentMetaInfo metaIno){
		metaIno.setAddInfos(new HashMap<>());
		Map<String, Object> customParams = header.getCustomParams();
		if (customParams != null) {
			metaIno.getAddInfos().putAll(customParams);
			if(customParams.containsKey("exp")){
				Object exp = customParams.get("exp");
				if(exp instanceof Long)metaIno.setExp(new Date((Long)exp));
			}
		}
		if(header.getCompressionAlgorithm()!=null)metaIno.setZip(header.getCompressionAlgorithm().getName());
		if(header.getContentType()!=null)metaIno.setContentTrype(header.getContentType());
		
	}
	
	public static final JWEHeader.Builder metaInfo2Header(final ContentMetaInfo metaIno, JWEHeader.Builder headerBuilder){
		// content type
		String contentTrype = metaIno.getContentTrype();
		if (StringUtils.isNotBlank(contentTrype)) {
			headerBuilder = headerBuilder.contentType(contentTrype);
		}

		String zip = metaIno.getZip();
		if (StringUtils.isNotBlank(zip)) {
			headerBuilder = headerBuilder.compressionAlgorithm(CompressionAlgorithm.DEF);
		} else {
			if (StringUtils.isNotBlank(contentTrype) && StringUtils.containsIgnoreCase(contentTrype, "text")) {
				headerBuilder = headerBuilder.compressionAlgorithm(CompressionAlgorithm.DEF);
			}
		}

		Map<String, Object> addInfos = metaIno.getAddInfos();
		// exp
		if (metaIno.getExp() != null) {
			if (addInfos == null)
				addInfos = new HashMap<>();
			addInfos.put("exp", metaIno.getExp().getTime());
		}

		if (addInfos != null) {
			headerBuilder = headerBuilder.customParams(addInfos);
		}
		return headerBuilder;
	}
}
