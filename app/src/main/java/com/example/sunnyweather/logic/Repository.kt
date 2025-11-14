package com.example.sunnyweather.logic

import android.util.Log
import androidx.lifecycle.liveData
import com.example.sunnyweather.logic.dao.PlaceDao
import com.example.sunnyweather.logic.model.Place
import com.example.sunnyweather.logic.model.Weather
import com.example.sunnyweather.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

object Repository{
    fun searchPlaces(query:String)=liveData(Dispatchers.IO){
        val result =try{
            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
            if(placeResponse.status == "ok"){
                val places =placeResponse.places
                Result.success(places)
            }else{
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
        }catch(e:Exception){
            Result.failure<List<Place>>(e)
        }
        emit(result)
    }

    // 提供了一个refreshweather()方法用来刷新天气信息
    fun refreshWeather(lng:String,lat:String)=liveData(Dispatchers.IO){
        val result=try{
            // 由于async函数必须在协程作用域内才能调用，所以这里又使用coroutineScope函数创建了一个协程作用域
            coroutineScope {
                // 在两个async函数中发起网络请求,再分别调用它们的await()方法
                delay(1500)     // 新版本由于控制了频率，所以必须加延时❗❗❗
                val deferredRealtime=async{
                    SunnyWeatherNetwork.getRealtimeWeather(lng,lat)
                }
                delay(1500)
                val deferredDaily=async{
                    Log.i("问题7的地方",lng+lat)
                    SunnyWeatherNetwork.getDailyWeather(lng,lat)
                }
//                Log.i("问题1的地方","开始请求数据了")

                val realtimeResponse=deferredRealtime.await()   // 这是3
//                Log.i("问题2的地方",realtimeResponse.toString())
//                delay(2000)
                Log.i("问题5的地方","开始请求数据了")
                val dailyResponse=deferredDaily.await()     // 这是2
                Log.i("问题6的地方",realtimeResponse.toString())
                // 两个await均获取到了结果之后，才会继续后面的逻辑
                if(realtimeResponse.status=="ok" && dailyResponse.status=="ok"){
                    // 将Realtime和Daily对象取出并封装到一个Weather对象中⭐
                    val weather= Weather(realtimeResponse.result.realtime,
                        dailyResponse.result.daily)
                    // 使用Result.success()方法来包装这个Weather对象
                    Result.success(weather)
                }else{
                    // 使用Result.failure()方法来包装一个异常信息
                    Result.failure<Weather>(
                        RuntimeException(
                            "realtime response status is ${realtimeResponse.status}"+
                                    "daily response status is ${dailyResponse.status}"
                        )
                    )
                }
            }
        }catch(e:Exception){
            Result.failure<Weather>(e)
        }
        // 最后调用 emit()方法将包装的结果发射出去
        Log.i("问题3的地方",result.toString())
        emit(result)
    }

    fun savePlace(place:Place)=PlaceDao.savePlace(place)
    fun getSavedPlace()=PlaceDao.getSavedPlace()
    fun isPlaceSaved()= PlaceDao.isPlaceSaved()

}