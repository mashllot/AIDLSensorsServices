/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\GITHUB\\AIDLSensorsServices\\src\\com\\zak\\aidlsensorsservices\\IGETGyroData.aidl
 */
package com.zak.aidlsensorsservices;
public interface IGETGyroData extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.zak.aidlsensorsservices.IGETGyroData
{
private static final java.lang.String DESCRIPTOR = "com.zak.aidlsensorsservices.IGETGyroData";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.zak.aidlsensorsservices.IGETGyroData interface,
 * generating a proxy if needed.
 */
public static com.zak.aidlsensorsservices.IGETGyroData asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.zak.aidlsensorsservices.IGETGyroData))) {
return ((com.zak.aidlsensorsservices.IGETGyroData)iin);
}
return new com.zak.aidlsensorsservices.IGETGyroData.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_getGyroXYZ:
{
data.enforceInterface(DESCRIPTOR);
float[] _result = this.getGyroXYZ();
reply.writeNoException();
reply.writeFloatArray(_result);
return true;
}
case TRANSACTION_getGyroDelay:
{
data.enforceInterface(DESCRIPTOR);
float _result = this.getGyroDelay();
reply.writeNoException();
reply.writeFloat(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.zak.aidlsensorsservices.IGETGyroData
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public float[] getGyroXYZ() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
float[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getGyroXYZ, _data, _reply, 0);
_reply.readException();
_result = _reply.createFloatArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public float getGyroDelay() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
float _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getGyroDelay, _data, _reply, 0);
_reply.readException();
_result = _reply.readFloat();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getGyroXYZ = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getGyroDelay = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
public float[] getGyroXYZ() throws android.os.RemoteException;
public float getGyroDelay() throws android.os.RemoteException;
}
