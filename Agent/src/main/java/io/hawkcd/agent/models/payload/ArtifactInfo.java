/*
 * Copyright (C) 2016 R&D Solutions Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.hawkcd.agent.models.payload;

import io.hawkcd.agent.enums.ArtifactInfoType;

import java.util.List;

public class ArtifactInfo {
    private String name;
    private String url;
    private ArtifactInfoType type;
    private List<ArtifactInfo> files;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ArtifactInfoType getType() {
        return type;
    }

    public void setType(ArtifactInfoType type) {
        this.type = type;
    }

    public List<ArtifactInfo> getFiles() {
        return files;
    }

    public void setFiles(List<ArtifactInfo> files) {
        this.files = files;
    }
}
