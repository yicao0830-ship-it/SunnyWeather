package com.example.sunnyweather.logic.network

import android.util.Log
import com.example.sunnyweather.logic.model.DailyResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object SunnyWeatherNetwork{
    private val placeService=ServiceCreator.create<PlaceService>()
    suspend fun searchPlaces(query:String)=placeService.searchPlaces(query).await("1111")

    private val weatherService=ServiceCreator.create(WeatherService::class.java)
    suspend fun getDailyWeather(lng:String,lat:String): DailyResponse {
        Log.i("问题8getDailyWeather", "请求每日天气数据: lng=$lng, lat=$lat")
        val result = weatherService.getDailyWeather(lng,lat).await("2222")
        Log.i("问题9getDailyWeather", "每日天气数据响应: $result")
        return result
    }
    suspend fun getRealtimeWeather(lng:String,lat:String)=
        weatherService.getRealtimeWeather(lng,lat).await("3333")
    private suspend fun<T>Call<T>.await(tag: String = ""):T{
        return suspendCoroutine{continuation->
            enqueue(object: Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>){
                    val body=response.body()
                    Log.i("问题4的地方",tag+body.toString())
                    if(body!=null)continuation.resume(body)
                    else continuation.resumeWithException(
                        RuntimeException("response body is null"))
                }
                override fun onFailure(call:Call<T>,t:Throwable){
                    continuation.resumeWithException(t)
                }
            })
        }
    }
}