# EEG Toolkit for Muse Android

EEG Toolkit is an Android library for interfacing with Muse devices. It aims to simplify the Muse SDK, making it easier to connect, process and view the data streaming from a device. It is designed to work with apps using [Android Data Binding](https://developer.android.com/topic/libraries/data-binding/index.html), and contains the following:

#### [ViewModels](https://github.com/padster/Muse-EEG-Toolkit/tree/master/eegtoolkit/src/main/java/eeg/useit/today/eegtoolkit/vm) (i.e. live data)
* MuseListViewModel, a live list of all Muse devices that have been detected over bluetooth.
* StreamingDeviceViewModel, a connection to a Muse device that is streaming data.
* TimeSeries, a live stream of (timestamp, value) that supports history and snapshots.
* SensorGoodViewModel, an observable set of the last isGood values for each sensor.
* FrequencyBandViewModel, an observable set of the last frequency band values for each sensor, with many options for which band values to use.
* RawChannelViewModel, which converts a single raw channel into its timeseries.

More are planned, both for values coming from the Muse (e.g. Frequency band time series) as well as ones that assist in processing the data by combining multiple sources.

#### [Views](https://github.com/padster/Muse-EEG-Toolkit/tree/master/eegtoolkit/src/main/java/eeg/useit/today/eegtoolkit/view)
* GraphSurfaceView, which draws a TimeSeries onto a surface view, as a scrolling line chart.
* GraphGLView, which does the same but onto a GLES 2.0 surface.

#### Sample App
In addition, a [sample app](https://github.com/padster/Muse-EEG-Toolkit/tree/master/sampleApp) is provided that shows example usage of the above:

![Device List](https://raw.githubusercontent.com/padster/Muse-EEG-Toolkit/master/images/deviceList.png)

Shows the list of devices found, current scanning status, and lets a user select a device.

![Device Details](https://raw.githubusercontent.com/padster/Muse-EEG-Toolkit/master/images/deviceDetails.png)

Shows the device connection status, the isGood for each sensor, and a scrolling graph of a raw channel as a SurfaceView and GLSurfaceView.

#### Future plans
The following list are some ideas of what will likely be added to the Toolkit:
* Time Series for each (sensor, frequency band) pair.
* Generic ViewModel that is just the last value of a time series.
* View that draws a histogram, and updates its heights live as more values come in.
* TimeSeries mergers, which allow you to take multiple time series and combine into new ones (e.g. average the four alpha values across the sensors, or divide alpha by beta, etc...)
* Optimize the live streaming VM to reuse listeners if multiple calls try listening to the same data source (e.g. one raw value from EEG1, one raw value from EEG2, should reuse the EEG listener).

The list is tentative however, and may change. If you have any suggestions, file an issue here. Pull requests also welcome to add anything that seems useful!
