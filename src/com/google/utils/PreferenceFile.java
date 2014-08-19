/**
 * 
 */
package com.google.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;

/**
 * The preference helper class used to save user data.
 */
public class PreferenceFile {

	private static Context sContext;

	private final int mMode;

	private final String mName;

	public PreferenceFile(String name, int mode) {
		mName = name;
		mMode = mode;
	}
	

	public static void init(Context context) {
		sContext = context;
	}

	public SharedPreferences open() {
		return sContext.getSharedPreferences(mName, mMode);
	}

	public static boolean commit(SharedPreferences.Editor editor) {
		if (Build.VERSION.SDK_INT < 9) {
			return editor.commit();
		}
		editor.apply();
		return true;
	}

	public SharedPreference<Boolean> value(String key, Boolean defaultValue) {
		return new SharedPreference<Boolean>(this, key, defaultValue) {

			@Override
			protected Boolean read(SharedPreferences sp) {
				if (sp.contains(mKey)) {
					return Boolean.valueOf(sp.getBoolean(mKey, false));
				}
				return mDefaultValue;
			}

			@Override
			protected void write(Editor editor, Boolean value) {
				if (value == null) {
					throw new IllegalArgumentException("null cannot be written for <Boolean>");
				}
				editor.putBoolean(mKey, value.booleanValue());
			}

		};
	}

	public SharedPreference<Integer> value(String key, Integer defaultValue) {
		return new SharedPreference<Integer>(this, key, defaultValue) {

			@Override
			protected Integer read(SharedPreferences sp) {
				if (sp.contains(mKey)) {
					return Integer.valueOf(sp.getInt(mKey, 0));
				}
				return mDefaultValue;
			}

			@Override
			protected void write(Editor editor, Integer value) {
				if (value == null) {
					throw new IllegalArgumentException("null cannot be written for <Integer>");
				}
				editor.putInt(mKey, value.intValue());
			}

		};
	}

	public SharedPreference<Long> value(String key, Long defaultValue) {
		return new SharedPreference<Long>(this, key, defaultValue) {

			@Override
			protected Long read(SharedPreferences sp) {
				if (sp.contains(mKey)) {
					return Long.valueOf(sp.getLong(mKey, 0));
				}
				return mDefaultValue;
			}

			@Override
			protected void write(Editor editor, Long value) {
				if (value == null) {
					throw new IllegalArgumentException("null cannot be written for <Long>");
				}
				editor.putLong(mKey, value.longValue());
			}

		};
	}

	public SharedPreference<String> value(String key, String defaultValue) {
		return new SharedPreference<String>(this, key, defaultValue) {

			@Override
			protected String read(SharedPreferences sp) {
				if (sp.contains(mKey)) {
					return sp.getString(mKey, null);
				}
				return mDefaultValue;
			}

			@Override
			protected void write(Editor editor, String value) {
				editor.putString(mKey, value);
			}
		};
	}

	public abstract class SharedPreference<T> {

		final T mDefaultValue;

		PreferenceFile mFile;

		final String mKey;

		protected SharedPreference(PreferenceFile file, String key, T defaultValue) {
			mFile = file;
			mKey = key;
			mDefaultValue = defaultValue;
		}

		public final boolean exists() {
			return mFile.open().contains(mKey);
		}

		public final T get() {
			return read(mFile.open());
		}

		public final String getKey() {
			return mKey;
		}

		public final void put(T value) {
			SharedPreferences sp = mFile.open();
			SharedPreferences.Editor editor = sp.edit();
			write(editor, value);
			commit(editor);
		}

		public final void remove() {
			commit(mFile.open().edit().remove(mKey));
		}

		protected abstract T read(SharedPreferences sp);

		protected abstract void write(SharedPreferences.Editor editor, T value);
	}
}
