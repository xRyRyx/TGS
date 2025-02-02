//package com.tgs.app.provinces
//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//
//class LocationViewModel(private val repository: LocationRepository) : ViewModel() {
//
//    private val _provinces = MutableLiveData<List<LocationModels.Province>>()
//    val provinces: LiveData<List<LocationModels.Province>> get() = _provinces
//
//    private val _cities = MutableLiveData<List<LocationModels.City>>()
//    val cities: LiveData<List<LocationModels.City>> get() = _cities
//
//    private val _municipalities = MutableLiveData<List<LocationModels.Municipality>>()
//    val municipalities: LiveData<List<LocationModels.Municipality>> get() = _municipalities
//
//    private val _barangays = MutableLiveData<List<LocationModels.Barangay>>()
//    val barangays: LiveData<List<LocationModels.Barangay>> get() = _barangays
//
//    private val _selectedProvince = MutableLiveData<String>()
//    val selectedProvince: LiveData<String> get() = _selectedProvince
//
//    private val _selectedCity = MutableLiveData<String>()
//    val selectedCity: LiveData<String> get() = _selectedCity
//
//    private val _selectedMunicipality = MutableLiveData<String>()
//    val selectedMunicipality: LiveData<String> get() = _selectedMunicipality
//
//    fun loadProvinces() {
//        viewModelScope.launch(Dispatchers.IO) {
//            val data = repository.fetchProvinces()
//            _provinces.postValue(data)
//        }
//    }
//
//    // In LocationViewModel
//    fun loadCities(provinceId: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            val data = repository.fetchCities()
//            val filteredCities = data.filter { it.id == provinceId }  // Make sure this matches
//            _cities.postValue(filteredCities)
//        }
//    }
//
//    fun loadMunicipalities(cityId: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            val data = repository.fetchMunicipalities()
//            val filteredMunicipalities = data.filter { it.id == cityId }  // Make sure this matches
//            _municipalities.postValue(filteredMunicipalities)
//        }
//    }
//
//    fun loadBarangays(municipalityId: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            val data = repository.fetchBarangays()
//            val filteredBarangays = data.filter { it.id == municipalityId }  // Make sure this matches
//            _barangays.postValue(filteredBarangays)
//        }
//    }
//
//    fun selectProvince(provinceId: String) {
//        _selectedProvince.value = provinceId
//        loadCities(provinceId)
//    }
//
//    fun selectCity(cityId: String) {
//        _selectedCity.value = cityId
//        loadMunicipalities(cityId)
//    }
//
//    fun selectMunicipality(municipalityId: String) {
//        _selectedMunicipality.value = municipalityId
//        loadBarangays(municipalityId)
//    }
//}
