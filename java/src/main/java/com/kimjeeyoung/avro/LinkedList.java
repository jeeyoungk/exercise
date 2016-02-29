/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package com.kimjeeyoung.avro;  
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class LinkedList extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = 6231379833340459913L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"LinkedList\",\"namespace\":\"com.kimjeeyoung.avro\",\"fields\":[{\"name\":\"value\",\"type\":\"string\"},{\"name\":\"next\",\"type\":[\"LinkedList\",\"null\"]}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public java.lang.CharSequence value;
  @Deprecated public com.kimjeeyoung.avro.LinkedList next;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>. 
   */
  public LinkedList() {}

  /**
   * All-args constructor.
   */
  public LinkedList(java.lang.CharSequence value, com.kimjeeyoung.avro.LinkedList next) {
    this.value = value;
    this.next = next;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return value;
    case 1: return next;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: value = (java.lang.CharSequence)value$; break;
    case 1: next = (com.kimjeeyoung.avro.LinkedList)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'value' field.
   */
  public java.lang.CharSequence getValue() {
    return value;
  }

  /**
   * Sets the value of the 'value' field.
   * @param value the value to set.
   */
  public void setValue(java.lang.CharSequence value) {
    this.value = value;
  }

  /**
   * Gets the value of the 'next' field.
   */
  public com.kimjeeyoung.avro.LinkedList getNext() {
    return next;
  }

  /**
   * Sets the value of the 'next' field.
   * @param value the value to set.
   */
  public void setNext(com.kimjeeyoung.avro.LinkedList value) {
    this.next = value;
  }

  /**
   * Creates a new LinkedList RecordBuilder.
   * @return A new LinkedList RecordBuilder
   */
  public static com.kimjeeyoung.avro.LinkedList.Builder newBuilder() {
    return new com.kimjeeyoung.avro.LinkedList.Builder();
  }
  
  /**
   * Creates a new LinkedList RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new LinkedList RecordBuilder
   */
  public static com.kimjeeyoung.avro.LinkedList.Builder newBuilder(com.kimjeeyoung.avro.LinkedList.Builder other) {
    return new com.kimjeeyoung.avro.LinkedList.Builder(other);
  }
  
  /**
   * Creates a new LinkedList RecordBuilder by copying an existing LinkedList instance.
   * @param other The existing instance to copy.
   * @return A new LinkedList RecordBuilder
   */
  public static com.kimjeeyoung.avro.LinkedList.Builder newBuilder(com.kimjeeyoung.avro.LinkedList other) {
    return new com.kimjeeyoung.avro.LinkedList.Builder(other);
  }
  
  /**
   * RecordBuilder for LinkedList instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<LinkedList>
    implements org.apache.avro.data.RecordBuilder<LinkedList> {

    private java.lang.CharSequence value;
    private com.kimjeeyoung.avro.LinkedList next;
    private com.kimjeeyoung.avro.LinkedList.Builder nextBuilder;

    /** Creates a new Builder */
    private Builder() {
      super(com.kimjeeyoung.avro.LinkedList.SCHEMA$);
    }
    
    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(com.kimjeeyoung.avro.LinkedList.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.value)) {
        this.value = data().deepCopy(fields()[0].schema(), other.value);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.next)) {
        this.next = data().deepCopy(fields()[1].schema(), other.next);
        fieldSetFlags()[1] = true;
      }
      if (other.hasNextBuilder()) {
        this.nextBuilder = com.kimjeeyoung.avro.LinkedList.newBuilder(other.getNextBuilder());
      }
    }
    
    /**
     * Creates a Builder by copying an existing LinkedList instance
     * @param other The existing instance to copy.
     */
    private Builder(com.kimjeeyoung.avro.LinkedList other) {
            super(com.kimjeeyoung.avro.LinkedList.SCHEMA$);
      if (isValidValue(fields()[0], other.value)) {
        this.value = data().deepCopy(fields()[0].schema(), other.value);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.next)) {
        this.next = data().deepCopy(fields()[1].schema(), other.next);
        fieldSetFlags()[1] = true;
      }
      this.nextBuilder = null;
    }

    /**
      * Gets the value of the 'value' field.
      * @return The value.
      */
    public java.lang.CharSequence getValue() {
      return value;
    }

    /**
      * Sets the value of the 'value' field.
      * @param value The value of 'value'.
      * @return This builder.
      */
    public com.kimjeeyoung.avro.LinkedList.Builder setValue(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.value = value;
      fieldSetFlags()[0] = true;
      return this; 
    }

    /**
      * Checks whether the 'value' field has been set.
      * @return True if the 'value' field has been set, false otherwise.
      */
    public boolean hasValue() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'value' field.
      * @return This builder.
      */
    public com.kimjeeyoung.avro.LinkedList.Builder clearValue() {
      value = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'next' field.
      * @return The value.
      */
    public com.kimjeeyoung.avro.LinkedList getNext() {
      return next;
    }

    /**
      * Sets the value of the 'next' field.
      * @param value The value of 'next'.
      * @return This builder.
      */
    public com.kimjeeyoung.avro.LinkedList.Builder setNext(com.kimjeeyoung.avro.LinkedList value) {
      validate(fields()[1], value);
      this.nextBuilder = null;
      this.next = value;
      fieldSetFlags()[1] = true;
      return this; 
    }

    /**
      * Checks whether the 'next' field has been set.
      * @return True if the 'next' field has been set, false otherwise.
      */
    public boolean hasNext() {
      return fieldSetFlags()[1];
    }

    /**
     * Gets the Builder instance for the 'next' field and creates one if it doesn't exist yet.
     * @return This builder.
     */
    public com.kimjeeyoung.avro.LinkedList.Builder getNextBuilder() {
      if (nextBuilder == null) {
        if (hasNext()) {
          setNextBuilder(com.kimjeeyoung.avro.LinkedList.newBuilder(next));
        } else {
          setNextBuilder(com.kimjeeyoung.avro.LinkedList.newBuilder());
        }
      }
      return nextBuilder;
    }

    /**
     * Sets the Builder instance for the 'next' field
     * @return This builder.
     */
    public com.kimjeeyoung.avro.LinkedList.Builder setNextBuilder(com.kimjeeyoung.avro.LinkedList.Builder value) {
      clearNext();
      nextBuilder = value;
      return this;
    }

    /**
     * Checks whether the 'next' field has an active Builder instance
     * @return True if the 'next' field has an active Builder instance
     */
    public boolean hasNextBuilder() {
      return nextBuilder != null;
    }

    /**
      * Clears the value of the 'next' field.
      * @return This builder.
      */
    public com.kimjeeyoung.avro.LinkedList.Builder clearNext() {
      next = null;
      nextBuilder = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    @Override
    public LinkedList build() {
      try {
        LinkedList record = new LinkedList();
        record.value = fieldSetFlags()[0] ? this.value : (java.lang.CharSequence) defaultValue(fields()[0]);
        if (nextBuilder != null) {
          record.next = this.nextBuilder.build();
        } else {
          record.next = fieldSetFlags()[1] ? this.next : (com.kimjeeyoung.avro.LinkedList) defaultValue(fields()[1]);
        }
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  private static final org.apache.avro.io.DatumWriter
    WRITER$ = new org.apache.avro.specific.SpecificDatumWriter(SCHEMA$);  

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, org.apache.avro.specific.SpecificData.getEncoder(out));
  }

  private static final org.apache.avro.io.DatumReader
    READER$ = new org.apache.avro.specific.SpecificDatumReader(SCHEMA$);  

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, org.apache.avro.specific.SpecificData.getDecoder(in));
  }

}
