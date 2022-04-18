package com.ahobsu.moti.presentation.ui.question

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ahobsu.moti.domain.AnswerUseCase
import com.ahobsu.moti.domain.MissionUseCase
import com.ahobsu.moti.domain.entity.Answer
import com.ahobsu.moti.domain.entity.Mission
import com.ahobsu.moti.domain.repository.AnswerRepository
import com.ahobsu.moti.domain.repository.MissionRepository
import com.ahobsu.moti.presentation.BaseViewModel
import com.ahobsu.moti.presentation.ui.question.model.AnswerModel
import com.ahobsu.moti.presentation.ui.question.model.MissionItemModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


class MissionViewModel(
    private val missionRepository: MissionRepository,
    private val answerRepository: AnswerRepository
) : BaseViewModel() {

    private val _missionList = MutableLiveData<List<MissionItemModel>>()
    val missionList: LiveData<List<MissionItemModel>> = _missionList

    private val _selectMission = MutableLiveData<MissionItemModel>()
    val selectMission: LiveData<MissionItemModel> = _selectMission


    private val _missionAnswer = MutableLiveData<AnswerModel>()
    val missionAnswer: LiveData<AnswerModel> = _missionAnswer

    val answerContent = MutableLiveData<String>()

    private val _backPressed = MutableLiveData<Unit>()
    val backPressed: LiveData<Unit> = _backPressed

    private val _complete = MutableLiveData<Unit>()
    val complete: LiveData<Unit> = _complete

    private val _getImage = MutableLiveData<Unit>()
    val getImage: LiveData<Unit> = _getImage

    fun initMission() {
        getMissions()
    }

    fun onClickAnswerImage() {
        _getImage.value = Unit
    }

    fun onClickComplete(context: Context) {
        postAnswer(context)
    }

    fun onClickBack() {
        _backPressed.value = Unit
    }

    fun onClickReset() {
        missionsRequest(MissionUseCase(missionRepository).getRefreshMissions())
    }

    fun setAnswerImage(img: Uri) {
        selectMission.value?.id?.let {
            val base = AnswerModel(
                content = _missionAnswer.value?.content,
                missionId = it,
                file = img
            )
            _missionAnswer.postValue(base)
        }
    }

    fun setAnswerContent() {
        Log.e("answerContent ", answerContent.value ?: "")
        answerContent.value ?: return
        selectMission.value?.id?.let {
            val base = AnswerModel(
                answerContent.value!!,
                it,
                _missionAnswer.value?.file
            )
            _missionAnswer.postValue(base)
        }
    }

    fun getMission(missionId: Int) {
        MissionUseCase(missionRepository).getMissionId(missionId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ it ->
                Log.e("getMissions ", it.toString())
                _selectMission.postValue(
                    MissionItemModel(
                        id = it.id ?: 0,
                        title = it.title ?: "",
                        isContent = it.isContent ?: true,
                        isImage = it.isImage ?: true
                    )
                )
            }, { e ->
                Log.e("e", e.toString())
            })
    }

    private fun postAnswer(context: Context) {
        selectMission.value?.id?.let {
            AnswerUseCase(answerRepository).postAnswer(
                Answer(
                    content = missionAnswer.value?.content,
                    file =
                    if (missionAnswer.value?.file == null) null
                    else createCopyAndReturnRealPath(
                        context,
                        missionAnswer.value?.file!!,
                        "photo.jpg"
                    ),
//                    file = missionAnswer.value?.file,
                    missionId = it
                )
            ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ it ->
                    _complete.postValue(Unit)
                }, { e ->
                    Log.e("e", e.toString())
                })
        }
    }

    private val tempFileList: ArrayList<String> = ArrayList()

    fun createCopyAndReturnRealPath(context: Context, uri: Uri, fileName: String): Uri? {
        val contentResolver: ContentResolver = context.contentResolver ?: return null
        // 내부 저장소 안에 위치하도록 파일 생성
        val filePath: String =
            context.applicationInfo.dataDir + File.separator + System.currentTimeMillis()
                .toString() + "." + fileName.substring(fileName.lastIndexOf(".") + 1)
        val file = File(filePath)
        // 서버에 이미지 업로드 후 삭제하기 위해 경로를 저장해둠
        tempFileList.add(filePath)
        try {
            // 매개변수로 받은 uri 를 통해  이미지에 필요한 데이터를 가져온다.
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            // 가져온 이미지 데이터를 아까 생성한 파일에 저장한다.
            val outputStream: OutputStream = FileOutputStream(file)
            val buf = ByteArray(1024)
            var len: Int
            while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
            outputStream.close()
            inputStream.close()
        } catch (ignore: IOException) {
            return null
        }
        return file.absolutePath.toUri() // 생성한 파일의 절대경로 반환
    }

    private fun getMissions() {
        missionsRequest((MissionUseCase(missionRepository).getMissions()))
    }

    private fun missionsRequest(req: Single<List<Mission>>) {
        req.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ it ->
                Log.e("getMissions ", it.toString())
                _missionList.postValue(mapMissionItem(it))
            }, { e ->
                Log.e("e", e.toString())
            })
    }

    private fun mapMissionItem(items: List<Mission>): List<MissionItemModel> {
        return items.mapIndexed { index, it ->
            MissionItemModel(
                id = it.id ?: 0,
                missionNum = (index + 1),
                title = it.title ?: "",
                isContent = it.isContent ?: true,
                isImage = it.isImage ?: true
            )
        }
    }
}