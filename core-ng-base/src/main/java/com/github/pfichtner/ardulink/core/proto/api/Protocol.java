/**
Copyright 2013 project Ardulink http://www.ardulink.org/
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.github.pfichtner.ardulink.core.proto.api;


public interface Protocol {

	String getName();

	byte[] getSeparator();

	interface FromArduino {
		// marker interface
	}

	byte[] toArduino(ToArduinoStartListening startListeningEvent);

	byte[] toArduino(ToArduinoStopListening stopListeningEvent);

	byte[] toArduino(ToArduinoPinEvent pinEvent);

	byte[] toArduino(ToArduinoKeyPressEvent charEvent);

	byte[] toArduino(ToArduinoTone tone);

	byte[] toArduino(ToArduinoNoTone noTone);

	byte[] toArduino(ToArduinoCustomMessage customMessage);

	FromArduino fromArduino(byte[] bytes);

}