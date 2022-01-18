// Copyright 2020 The Defold Foundation
// Licensed under the Defold License version 1.0 (the "License"); you may not use
// this file except in compliance with the License.
//
// You may obtain a copy of the License, together with FAQs at
// https://www.defold.com/license
//
// Unless required by applicable law or agreed to in writing, software distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied. See the License for the
// specific language governing permissions and limitations under the License.

package com.dynamo.bob.pipeline;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.FilenameUtils;

import com.dynamo.bob.Builder;
import com.dynamo.bob.BuilderParams;
import com.dynamo.bob.CompileExceptionError;
import com.dynamo.bob.ProtoParams;
import com.dynamo.bob.Task.TaskBuilder;
import com.dynamo.bob.Task;
import com.dynamo.bob.fs.IResource;
import com.dynamo.bob.pipeline.BuilderUtil;

import com.google.protobuf.TextFormat;

import com.dynamo.gamesys.proto.TextureSetProto.TextureSet;
import com.dynamo.gamesys.proto.Tile.TileLayer;
import com.dynamo.gamesys.proto.Tile.TileCell;
import com.dynamo.gamesys.proto.Tile.TileGrid;
import com.dynamo.tiled.proto.Tiled.TiledDesc;

import com.dynamo.bob.pipeline.Tiled;

@BuilderParams(name="Tiled", inExts=".tiled", outExt=".tilemapc")
public class TiledBuilder extends Builder<Void> {

    @Override
    public Task<Void> create(IResource input) throws IOException, CompileExceptionError {
        TaskBuilder<Void> taskBuilder = Task.<Void>newBuilder(this).setName(params.name());
        taskBuilder.addInput(input);
        taskBuilder.addOutput(input.changeExt(params.outExt()));
        return taskBuilder.build();
    }

    @Override
    public void build(Task<Void> task) throws CompileExceptionError, IOException {
        TiledDesc.Builder builder = TiledDesc.newBuilder();
        TextFormat.merge(new InputStreamReader(new ByteArrayInputStream(task.input(0).getContent())), builder);
        TiledDesc tiled = builder.build();
        String tmxPath = tiled.getTmx();
        String tilesetPath = tiled.getTileSet();
        String materialPath = tiled.getMaterial();

        BuilderUtil.checkResource(this.project, task.input(0), "tmx", tmxPath);
        BuilderUtil.checkResource(this.project, task.input(0), "tilesource", tilesetPath);
        BuilderUtil.checkResource(this.project, task.input(0), "material", materialPath);

        IResource tmx = this.project.getResource(tmxPath);
        final String tmxXml = new String(tmx.getContent());

        TileGrid.Builder tileGridBuilder = TileGrid.newBuilder();
        tileGridBuilder.setTileSet(BuilderUtil.replaceExt(tilesetPath, ".tilesource", ".texturesetc"));
        tileGridBuilder.setMaterial(BuilderUtil.replaceExt(materialPath, ".material", ".materialc"));

        final int width = Tiled.parseTmxGetWidth(tmxXml);
        final int height = Tiled.parseTmxGetHeight(tmxXml);

        for (int i = 0; i <= 0; i++) {
            TileLayer.Builder tileLayerBuilder = TileLayer.newBuilder();
            tileLayerBuilder.setId("layer" + i);
            tileLayerBuilder.setIsVisible(1);
            tileLayerBuilder.setZ(i);

            final String layer = Tiled.parseTmxGetLayerData(tmxXml, i);
            final String[] lines = layer.split("\n");
            int y = height - 1;
            for (String line : lines) {
                if (line.length() > 0) {
                    final String[] cells = line.split(",");
                    for (int x = 0; x < width; x++) {
                        final int cell = Integer.parseInt(cells[x]);
                        if (cell > 0) {
                            TileCell.Builder tileCellBuilder = TileCell.newBuilder();
                            tileCellBuilder.setX(x);
                            tileCellBuilder.setY(y);
                            tileCellBuilder.setTile(cell - 1);
                            tileLayerBuilder.addCell(tileCellBuilder.build());
                        }
                    }
                    y = y - 1;
                }
            }
            tileGridBuilder.addLayers(tileLayerBuilder.build());
        }
        // tileGridBuilder.setBlendMode(com.dynamo.gamesys.proto.Tile.TileGrid.BlendMode);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        tileGridBuilder.build().writeTo(out);
        out.close();
        task.output(0).setContent(out.toByteArray());
    }
}
