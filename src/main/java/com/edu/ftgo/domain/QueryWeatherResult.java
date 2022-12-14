package com.edu.ftgo.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class QueryWeatherResult {

  @NonNull
  private String city;

  @NonNull
  private String result;
}
