package com.just.donate.utils

import java.util.Optional

/**
 * Interface for objects that can be split up into two parts by a split value. The split value is used to determine
 * how much of the object is split off and how much remains. Optionally nothing remains and some split value remains
 * open.
 *
 * @tparam T The type of the object that can be split.
 * @tparam S The type of the object that determines the split, and which can be open if it's not covered.
 */
trait Splittable[T, S]:

  /**
   * Splits the object based on the provided split value.
   *
   * @param s The split value.
   * @return A Split instance containing the result of the split.
   */
  def splitOf(s: S): Splittable.Split[T, S]

object Splittable:

  /**
   * Class representing the result of a split operation.
   *
   * @param split  An Optional containing the split part, if present.
   * @param remain An Optional containing the remaining part, if present.
   * @param open   An Optional containing the open split value, if present.
   * @tparam T The type of the object that was split.
   * @tparam S The type of the split value.
   */
  case class Split[T, S](
    split: Optional[T] = Optional.empty(),
    remain: Optional[T] = Optional.empty(),
    open: Optional[S] = Optional.empty()
  ):

    /**
     * Auxiliary constructor to create a Split with non-optional parameters.
     *
     * @param split  The split part.
     * @param remain The remaining part.
     * @param open   The open split value.
     */
    def this(split: T, remain: T, open: S) = this(
      Optional.ofNullable(split),
      Optional.ofNullable(remain),
      Optional.ofNullable(open)
    )

    /**
     * Checks if the split results in a full remain without any split or open parts.
     *
     * @return True if only the remain part is present.
     */
    def fullRemain: Boolean = split.isEmpty && remain.isPresent && open.isEmpty

    /**
     * Checks if there is some split and remain without any open part.
     *
     * @return True if both split and remain are present.
     */
    def someSplit: Boolean = split.isPresent && remain.isPresent && open.isEmpty

    /**
     * Checks if the entire object is split without any remaining or open parts.
     *
     * @return True if only the split part is present.
     */
    def fullSplit: Boolean = split.isPresent && remain.isEmpty && open.isEmpty

    /**
     * Checks if the split is full and also has an open part.
     *
     * @return True if split and open are present.
     */
    def fullOpenSplit: Boolean = split.isPresent && remain.isEmpty && open.isPresent
