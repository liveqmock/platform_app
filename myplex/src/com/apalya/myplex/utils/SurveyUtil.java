/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.apalya.myplex.utils;

import android.app.Activity;
import android.util.Log;

import com.apalya.myplex.data.myplexapplication;
import com.mixpanel.android.mpmetrics.Survey;
import com.mixpanel.android.mpmetrics.SurveyCallbacks;

public class SurveyUtil {

	private static SurveyUtil self = null;
	private Activity activity = null;

	private enum SurveyState {
		UNDEFINED, INPROGRESS, COMPLETED
	};

	private SurveyState mSurveyState = SurveyState.UNDEFINED;

	private SurveyUtil() {
	}

	public static SurveyUtil getInstance() {
		if (self == null) {
			self = new SurveyUtil();
		}
		return self;
	}

	public interface SurveyListener {
		boolean canShowSurvey();
	}

	private SurveyListener surveyListener;

	public void setSurveyListener(SurveyListener surveyListener) {
		this.surveyListener = surveyListener;
	}

	public void checkForSurvey(Activity caller) {
		this.activity = caller;

		if (mSurveyState != SurveyState.UNDEFINED) {
			Log.d("MixpanelAPI",
					"discard survey check request already in-progress");
			return;
		}

		mSurveyState = SurveyState.INPROGRESS;

		myplexapplication.getMixPanel().getPeople()
				.checkForSurvey(new SurveyCallbacks() {
					
					public void foundSurvey(Survey s) {
						
						try {
							
							mSurveyState = SurveyState.COMPLETED;
							
							if (s == null || surveyListener == null) {
								return;
							}

							if (!surveyListener.canShowSurvey()) {
								Log.d("MixpanelAPI",
										"discard survey canShowSurvey returned false");
								mSurveyState = SurveyState.UNDEFINED;
								return;
							}

							myplexapplication.getMixPanel().getPeople()
									.showSurvey(s, activity);
							activity = null;

						} catch (Throwable e) {
							Log.d("MixpanelAPI",
									"exception after survey found :" + e);
						}
					}
				});
	}
}
