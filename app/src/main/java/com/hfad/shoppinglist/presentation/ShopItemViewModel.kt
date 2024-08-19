package com.hfad.shoppinglist.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.shoppinglist.data.ShopListRepositoryImpl
import com.hfad.shoppinglist.domain.AddShopItemUseCase
import com.hfad.shoppinglist.domain.EditShopItemUseCase
import com.hfad.shoppinglist.domain.GetShopItemUseCase
import com.hfad.shoppinglist.domain.ShopItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

class ShopItemViewModel @Inject constructor(
    private val getShopItemUseCase: GetShopItemUseCase,
    private val addShopItemUseCase: AddShopItemUseCase,
    private val editShopItemUseCase: EditShopItemUseCase
) : ViewModel() {

    private val _errorInputName = MutableLiveData<Boolean>()
    val errorInputName: LiveData<Boolean>
        get() = _errorInputName

    private val _errorInputCount = MutableLiveData<Boolean>()
    val errorInputCount: LiveData<Boolean>
        get() = _errorInputCount

    /* Было бы удобно, когда мы обращаемся к этому объекту из активити, то у неё был бы родительский тип LiveData, а когда работает с ним во вьюМодели, то
    был бы тип MutableLiveData. Тогда бы из ВьюМодели мы бы могли устанавливать значение, а из активити - нет. На джава мы бы могли написать таким образом:

        private MutableLiveData error = new MutableLiveData()
    public LiveData getError(){
        return error;
    }

    В этом случае, во вьюМодели мы бы работали с переменной error. Из активити мы бы к этой переменной обратиться не могли, поскольку она приватная. И вызвали бы геттер, который
    приведёт данный объект MutableLiveData к родительскому типу LiveData, но в котлин мы так сделать не можем.

    Если вы попытаетесь добавить геттер вручную

    fun getErrorInputName(): LiveData<Boolean>{
    то у вас возникнет ошибка, поскольку геттер на это поле создали за вас, и если вы самостоятельно добавляете новую функцию с таким же именем, то возникает ошибка.
    Компилятор не знает какой из этих методов вызывать когда вы обращаетесь к данному полю.

    Остается 2 варианта:
    1) давать геттерам какое-то новое имя, и там возвращать значение переменной (приводля её к LiveData)

    private getErrorInputNameLD(): LiveData<Boolean> {
    return errorInputName
    }

    но так обычно никто не делает. В большинстве компаний используют следующий подход:

    Создается приватная переменная, которая хранит ссылку на MutableLiveData, и с ней мы работаем только из ВьюМодели, при этом, перед названием
    данной переменной ставится знак нижнего подчеркивания _errorInputName, затем мы уже создаем публичную переменную, которая называется errorInputName
    Она будет типа LiveData, и у неё мы переопределяем геттер и этот геттер будет возвращать значение переменной _errorInputName

        private val _errorInputName = MutableLiveData<Boolean>()
    val errorInputName: LiveData<Boolean>
        get() = _errorInputName

        Теперь с Mutable переменной вы можете работать только из ВьюМодели, поскольку она приватная, и обращаясь к этому полю _errorInputName можно устанавливать значение

        а из активити мы будем подписываться на данную переменную (errorInputName)

        Теперь которой нельзя уже установить новое значение, поскольку здесь происходит апкаст(привидение к родительскому типу) LiveData:
            val errorInputName: LiveData<Boolean>
        get() = _errorInputName

        у которой устанавливать значение нельзя
     */


    private val _shopItem = MutableLiveData<ShopItem>()
    val shopItem: LiveData<ShopItem>
        get() = _shopItem

    private val _shouldCloseScreen = MutableLiveData<Unit>()
    val shouldCloseScreen: LiveData<Unit>
        get() = _shouldCloseScreen

    fun getShopItem(shopItemId: Int) {
        viewModelScope.launch {
            val item = getShopItemUseCase.getShopItem(shopItemId)
            _shopItem.value = item
        }
    }

    fun addShopItem(inputName: String?, inputCount: String?) {
        val name = parseName(inputName)
        val count = parseCount(inputCount)
        val fieldsValid = validateInput(name, count)
        if (fieldsValid) {
            viewModelScope.launch {
                val shopItem = ShopItem(name, count, true)
                addShopItemUseCase.addShopItem(shopItem)
                finishWork()
            }
        }
    }

    fun editShopItem(inputName: String?, inputCount: String?) {
        val name = parseName(inputName)
        val count = parseCount(inputCount)
        val fieldsValid = validateInput(name, count)
        if (fieldsValid) {
            _shopItem.value?.let {
                viewModelScope.launch {
                    val item = it.copy(name = name, count = count)
                    editShopItemUseCase.editShopItem(item)
                    finishWork()
                }
            }
        }
        /*В этом методе мы получаем имя и количество, проверяем, что поля введены правильно, и если они введены корректно, тогда мы получаем объект
        из LiveData, и если он там есть, и не равен null, то выполняется код внутри фигурных скобок. мы создаем новый объект, путём копирования уже существующего, и у нового
        объекта устанавливаем новое значение name и count. После того, как мы получили этот объект , мы его отправляем в качестве параметра в метод editShopItem
        после чего завершаем работу finishWork()
         */
    }

    private fun parseName(inputName: String?): String {
        return inputName?.trim() ?: ""
    }

    private fun parseCount(inputCount: String?): Int {
        return try {
            inputCount?.trim()?.toInt() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun validateInput(name: String, count: Int): Boolean {
        var result = true
        if (name.isBlank()) {
            _errorInputName.value = true
            result = false
        }
        if (count <= 0) {
            _errorInputCount.value = true
            result = false
        }
        return result
    }

    fun resetErrorInputName() {
        _errorInputName.value = false
    }

    fun resetErrorInputCount() {
        _errorInputCount.value = false
    }

    private fun finishWork() {
        _shouldCloseScreen.value = Unit
    }
}