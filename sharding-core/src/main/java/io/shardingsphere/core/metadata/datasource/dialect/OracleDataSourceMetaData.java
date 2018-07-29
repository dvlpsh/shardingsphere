/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.metadata.datasource.dialect;

import com.google.common.base.Splitter;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.metadata.datasource.DataSourceMetaData;
import lombok.Getter;

import java.net.URI;
import java.util.List;

/**
 * Data source meta data for Oracle.
 *
 * @author panjuan
 */
@Getter
public final class OracleDataSourceMetaData implements DataSourceMetaData {
    
    private static final int DEFAULT_PORT = 1521;
    
    private final String hostName;
    
    private final int port;
    
    private final String schemeName;
    
    public OracleDataSourceMetaData(final String url) {
        URI uri = getURI(url);
        hostName = uri.getHost();
        port = -1 == uri.getPort() ? DEFAULT_PORT : uri.getPort();
        schemeName = uri.getPath().isEmpty() ? "" : uri.getPath().substring(1);
    }
    
    private URI getURI(final String url) {
        String cleanUrl = url.substring(5);
        if (cleanUrl.contains("oracle:thin:@//")) {
            cleanUrl = cleanUrl.replace("oracle:thin:@//", "oracle://");
        } else if (cleanUrl.contains("oracle:thin:@")) {
            cleanUrl = cleanUrl.replace("oracle:thin:@", "oracle://");
        }
        List<String> parts = Splitter.on(":").splitToList(cleanUrl);
        if (4 == parts.size()) {
            cleanUrl = parts.get(0) + ":" + parts.get(1) + ":" + parts.get(2) + "/" + parts.get(3);
        }
        URI result = URI.create(cleanUrl);
        if (null == result.getHost()) {
            throw new ShardingException("The URL of JDBC is not supported.");
        }
        return result;
    }
    
    @Override
    public boolean isInSameDatabaseInstance(final DataSourceMetaData dataSourceMetaData) {
        return hostName.equals(dataSourceMetaData.getHostName()) && port == dataSourceMetaData.getPort();
    }
}
