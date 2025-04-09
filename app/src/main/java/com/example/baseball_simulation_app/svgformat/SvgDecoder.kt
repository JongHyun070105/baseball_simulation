package com.example.baseball_simulation_app.svgformat

import android.util.Log
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.caverock.androidsvg.SVG
import java.io.InputStream

class SvgDecoder : ResourceDecoder<InputStream, SVG> {
    override fun handles(source: InputStream, options: Options): Boolean {
        // 모든 InputStream을 처리한다고 가정
        return true
    }

    override fun decode(source: InputStream, width: Int, height: Int, options: Options): Resource<SVG>? {
        return try {
            val svg = SVG.getFromInputStream(source)
            SimpleResource(svg)
        } catch (ex: Exception) {
            Log.e("SvgDecoder", "Cannot load SVG from stream", ex)
            null
        }
    }
}
